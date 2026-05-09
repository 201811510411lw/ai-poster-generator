# AI 平面物料生成系统架构设计

本文档基于当前代码实现更新。当前 MVP 采用 Vue 前端 + Java Spring Boot 主后端 + MySQL + OpenAI 图片生成接口。Java 后端直接调用 OpenAI 图片模型，不再要求独立的 Python AI 服务。

## 1. 架构目标

系统第一阶段目标是跑通以下核心闭环：

```text
用户登录
  ↓
上传素材
  ↓
填写颜色、文案、物料类型、尺寸和设计要求
  ↓
创建 AI 生成任务
  ↓
Java 后端调用图像生成模型
  ↓
保存生成结果
  ↓
前端预览和下载
```

系统不是桌面端软件，也不是 ComfyUI 节点式工具，而是一个面向电脑端浏览器使用的 AI 平面物料生成 Web 系统。

## 2. 当前总体技术架构

当前 MVP 推荐采用两层应用架构：

```text
Vue 前端
  ↓ HTTP / JSON / multipart
Java Spring Boot 主后端
  ├─ 登录 / JWT
  ├─ 用户信息
  ├─ 素材上传和元数据管理
  ├─ 海报生成任务
  ├─ Prompt 组装
  ├─ 直接调用 OpenAI 图片生成接口
  ├─ 生成图片保存
  └─ 历史记录查询

MySQL
  └─ 用户、素材、任务、任务素材关联

本地文件系统 / 对象存储
  └─ 上传素材和生成图片
```

当前实际生成链路：

```text
POST /api/posters/generate
  ↓
PosterController
  ↓
PosterService 校验参数、读取素材、创建任务
  ↓
PosterPromptBuilder 组装 prompt
  ↓
OpenAiImageClient 调用 /v1/images/generations
  ↓
Java 解码 OpenAI 返回的 b64_json
  ↓
StorageService 保存生成图片
  ↓
更新任务状态 SUCCESS / FAILED
  ↓
返回 taskId、status、imageUrl、width、height
```

其中：

- Vue 负责页面展示、表单交互、素材选择、生成状态展示和调用 Java 后端接口。
- Java 负责登录、用户、权限、素材、任务、Prompt、模型调用、数据库、文件存储等主业务。
- MySQL 保存用户、素材、生成任务、任务素材关联等业务数据。
- 本地文件系统、MinIO、OSS、TOS 或 S3 可以保存上传素材和生成图片。
- Python AI 服务当前不是 MVP 必需组件，仅作为未来扩展选项。

## 3. 当前目录结构

```text
ai-poster-generator/
  backend-java/
    pom.xml
    src/main/java/com/aiposter/
      PosterAdminApplication.java
      common/
      config/
      security/
      auth/
      user/
      asset/
      poster/
      openai/
      storage/
    src/main/resources/
      application.yml
      db/init.sql

  frontend/
    package.json
    src/
      api/
      router/
      stores/
      views/
        login/
        poster-generator/

  docs/
    architecture.md
```

如果后续需要接入 ComfyUI、Stable Diffusion/Flux 本地模型、复杂图片后处理或多模型网关，可以再新增 `ai-service-python/`，但当前不建议为了 MVP 提前引入。

## 4. 各层职责边界

### 4.1 Vue 前端

前端只负责页面展示、表单交互和调用 Java 后端接口，不直接访问 OpenAI、Grok、本地模型或对象存储私有接口。

主要职责：

1. 登录页。
2. 主工作台布局。
3. 素材上传区。
4. 基础配置区。
5. 生成参数区。
6. 结果预览区。
7. 任务历史展示。
8. token 保存和请求拦截。
9. 401 跳转登录页。

当前主要前端接口文件：

```text
frontend/src/api/request.ts          # Axios 实例、token、错误处理
frontend/src/api/auth.ts             # 登录、当前用户、退出登录
frontend/src/api/poster.ts           # 素材、生成、历史接口
frontend/src/router/index.ts         # 路由和登录守卫
frontend/src/stores/auth.ts          # 登录用户状态
frontend/src/stores/posterGenerator.ts # 海报生成表单、素材和历史状态
```

当前前端调用的主要后端接口：

```http
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout

POST   /api/assets/upload
GET    /api/assets
PUT    /api/assets/{assetId}/type
DELETE /api/assets/{assetId}

POST /api/posters/generate
GET  /api/posters/history
```

### 4.2 Java Spring Boot 主后端

Java 后端是系统的业务中心。前端所有业务请求都先进入 Java 后端。

主要职责：

1. 登录认证。
2. JWT 签发和校验。
3. 用户信息管理。
4. 素材上传、校验和保存。
5. 素材元数据入库。
6. 海报生成任务创建。
7. 参数校验。
8. 物料类型和模型尺寸规则管理。
9. Prompt 组装。
10. 直接调用 OpenAI 图片生成接口。
11. 解码并保存生成结果。
12. 任务状态更新。
13. 历史记录查询。
14. 统一异常处理和接口返回。

当前 Java 包结构重点：

```text
common/
  ApiResponse.java
  BusinessException.java
  GlobalExceptionHandler.java

security/
  JwtTokenProvider.java
  JwtAuthenticationFilter.java
  SecurityConfig.java
  LoginUser.java

auth/
  AuthController.java
  AuthService.java
  dto/LoginRequest.java
  dto/LoginResponse.java
  dto/CurrentUserResponse.java

user/
  UserEntity.java
  UserRepository.java

asset/
  AssetController.java
  AssetService.java
  AssetEntity.java
  AssetRepository.java
  dto/AssetUploadResponse.java

poster/
  PosterController.java
  PosterService.java
  PosterPromptBuilder.java
  PosterTaskEntity.java
  PosterTaskRepository.java
  PosterGenerationAssetEntity.java
  PosterGenerationAssetRepository.java
  dto/GeneratePosterRequest.java
  dto/GeneratePosterResponse.java
  dto/PosterHistoryItemResponse.java

openai/
  OpenAiImageClient.java
  OpenAiImageProperties.java

storage/
  StorageService.java
  LocalStorageService.java
  StoredFile.java
```

### 4.3 Python AI 服务的定位

当前 MVP 不需要 Python AI 服务。

后续只有在出现以下需求时，才建议重新引入 `ai-service-python/`：

1. 接入 ComfyUI 工作流。
2. 接入 Stable Diffusion、Flux 等本地模型。
3. 做复杂图像后处理，例如抠图、扩图、超分、批量压缩、格式转换。
4. 构建多模型网关，对接 OpenAI、Grok、ComfyUI、本地模型等多个 provider。
5. Java SDK 或 HTTP 调用无法满足某些模型能力，而 Python 生态更合适。

引入 Python 服务后的推荐职责是只处理 AI 能力和图片处理，不直接管理用户、权限、任务主数据和业务数据库。

## 5. 登录系统设计

正式登录不应该只在前端 mock。账号、密码哈希和用户信息应保存在 MySQL，由 Java 后端完成校验。

### 5.1 登录流程

```text
Vue 登录页
  ↓
POST /api/auth/login
  ↓
Java 查询 MySQL 用户
  ↓
BCrypt 校验密码
  ↓
生成 JWT token
  ↓
返回 token 和用户信息
  ↓
Vue 保存 token
  ↓
GET /api/auth/me
  ↓
进入 /poster-generator
```

### 5.2 登录相关接口

```http
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
```

### 5.3 用户表

```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64),
  avatar VARCHAR(255),
  role VARCHAR(32) DEFAULT 'USER',
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

密码必须使用 BCrypt 等安全哈希保存，不能明文保存。

## 6. 素材上传设计

素材由 Java 后端接收并保存。

### 6.1 上传流程

```text
Vue 上传图片
  ↓
POST /api/assets/upload multipart/form-data
  ↓
Java 校验格式和大小
  ↓
Java 保存到本地或对象存储
  ↓
Java 读取图片尺寸
  ↓
写入 poster_asset 表
  ↓
返回 assetId 和访问 URL
```

### 6.2 上传限制

第一期建议：

- 单张图片不超过 20MB。
- 单次最多 10 张。
- 支持 PNG、JPG、JPEG。
- 记录素材类型：product、logo、decoration、background、reference、other。

### 6.3 素材表

```sql
CREATE TABLE poster_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  asset_type VARCHAR(32) NOT NULL,
  filename VARCHAR(255) NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  content_type VARCHAR(64),
  file_size BIGINT,
  width INT,
  height INT,
  storage_path VARCHAR(512) NOT NULL,
  access_url VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_poster_asset_user_created (user_id, created_at),
  CONSTRAINT fk_poster_asset_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
```

## 7. 海报生成任务设计

生成任务由 Java 创建、执行和管理。当前 MVP 使用同步生成：`POST /api/posters/generate` 会等待 OpenAI 图片接口返回结果后再响应前端。

### 7.1 当前任务流程

```text
Vue 点击生成
  ↓
POST /api/posters/generate
  ↓
Java 校验参数
  ↓
Java 查询并校验用户选择的素材
  ↓
Java 创建 poster_generation_task，状态 PENDING
  ↓
Java 保存 poster_generation_asset 关联
  ↓
Java 组装业务 prompt
  ↓
Java 调用 OpenAI /v1/images/generations
  ↓
OpenAI 返回 b64_json
  ↓
Java 解码图片并保存到 StorageService
  ↓
Java 更新任务为 SUCCESS 或 FAILED
  ↓
Vue 展示生成结果并刷新历史记录
```

### 7.2 Java 对前端接口

```http
POST /api/posters/generate
GET  /api/posters/history
```

后续如果生成时间变长，可以扩展为异步任务：

```http
POST /api/posters/generate       # 创建任务并立即返回 taskId
GET  /api/posters/tasks/{taskId} # 查询任务状态
GET  /api/posters/tasks          # 查询任务列表
GET  /api/posters/tasks/{taskId}/download
```

### 7.3 Java 调 OpenAI 图片接口

当前由 `OpenAiImageClient` 直接调用：

```http
POST /v1/images/generations
```

请求参数由 Java 配置和业务参数共同决定：

```json
{
  "model": "gpt-image-1",
  "prompt": "Create a polished commercial Chinese poster...",
  "size": "1024x1536",
  "quality": "medium"
}
```

接口返回的 `b64_json` 由 Java 解码并保存为生成图片。

### 7.4 任务表

当前任务表为 `poster_generation_task`：

```sql
CREATE TABLE poster_generation_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  material_type VARCHAR(32) NOT NULL,
  width INT NOT NULL,
  height INT NOT NULL,
  main_color VARCHAR(32),
  sub_color VARCHAR(32),
  brand_description VARCHAR(1024),
  style_description VARCHAR(1024),
  title VARCHAR(255),
  subtitle VARCHAR(255),
  activity_description VARCHAR(1024),
  design_requirement VARCHAR(2048),
  output_format VARCHAR(16) NOT NULL DEFAULT 'png',
  prompt_text TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  result_filename VARCHAR(255),
  result_storage_path VARCHAR(512),
  result_image_url VARCHAR(512),
  error_message VARCHAR(1024),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

任务和素材通过 `poster_generation_asset` 关联：

```sql
CREATE TABLE poster_generation_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  asset_id BIGINT NOT NULL,
  asset_role VARCHAR(32) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

状态建议：

```text
PENDING
SUCCESS
FAILED
```

如果后续改异步生成，可以增加：

```text
GENERATING
CANCELED
```

## 8. Prompt 组装职责

当前 Prompt 由 Java 的 `PosterPromptBuilder` 负责组装。

Java 会根据以下信息生成结构化 prompt：

1. 物料类型。
2. 画布尺寸。
3. 主色和辅助色。
4. 品牌描述。
5. 视觉风格。
6. 标题。
7. 副标题。
8. 活动说明。
9. 设计要求。
10. 用户选择的素材摘要。

当前素材只作为文本摘要进入 prompt，例如素材类型、原文件名和图片尺寸。模型还没有直接看到用户上传的图片内容。

这一点非常重要：

```text
当前实现 = 文本生成图片
不是 = 基于上传图片的图像编辑 / 图像参考生成
```

如果产品要求“必须使用上传的商品图或 logo”，下一步需要改造 `OpenAiImageClient`，接入支持图片输入的模型接口，把用户上传的素材文件作为真实图片输入传给模型。

## 9. 配置设计

### 9.1 Java application.yml

当前主要配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/ai_poster_generator?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 200MB

jwt:
  secret: ${JWT_SECRET:change-me-change-me-change-me-change-me-change-me-change-me}
  expiration-seconds: ${JWT_EXPIRATION_SECONDS:604800}

storage:
  type: ${STORAGE_TYPE:local}
  local:
    base-path: ${STORAGE_BASE_PATH:./data/uploads}
    public-base-url: ${STORAGE_PUBLIC_BASE_URL:/files}

openai:
  image:
    api-key: ${OPENAI_API_KEY:}
    base-url: ${OPENAI_BASE_URL:https://api.openai.com}
    model: ${OPENAI_IMAGE_MODEL:gpt-image-1}
    size: ${OPENAI_IMAGE_SIZE:auto}
    quality: ${OPENAI_IMAGE_QUALITY:medium}

app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

密钥不要提交到 GitHub。仓库中只保留 `.env.example` 或环境变量说明。

## 10. MVP 阶段拆分

### 第 1 阶段：前端登录闭环

1. 登录页。
2. token 保存。
3. 路由守卫。
4. 顶部用户信息。
5. 退出登录。

### 第 2 阶段：Java 登录和 MySQL

1. Spring Boot 项目初始化。
2. MySQL 用户表。
3. BCrypt 密码。
4. JWT 登录。
5. `/api/auth/login`。
6. `/api/auth/me`。
7. 前端 mock 登录改为真实接口。

### 第 3 阶段：素材上传

1. `/api/assets/upload`。
2. 文件格式和大小校验。
3. 本地存储。
4. 图片元数据入库。
5. 前端上传组件接真实接口。

### 第 4 阶段：Java 图片生成闭环

1. `/api/posters/generate`。
2. Java 创建任务。
3. Java 组装 prompt。
4. Java 直接调用 OpenAI 图片生成接口。
5. Java 保存结果图片。
6. 前端展示结果并刷新历史记录。

### 第 5 阶段：增强素材参与生成

1. 让上传素材作为真实图片输入传给模型。
2. 支持商品图、logo、参考图等不同素材角色。
3. 根据模型能力选择 image edit / image reference / multi-image input 方案。
4. 处理模型输出格式、真实图片尺寸和用户目标画布尺寸的差异。

### 第 6 阶段：可选 Python AI 服务

只有在需要本地模型、ComfyUI、复杂后处理或多模型网关时，再新增 Python AI 服务。

## 11. 与 requirement.txt 的关系

`requirement.txt` 主要描述产品需求、页面结构、字段、接口初稿和验收标准。

本文档补充系统工程设计，主要解决：

1. 当前采用 Vue + Java + MySQL + OpenAI 图片接口的技术路线。
2. 明确登录、用户、素材、任务、模型调用的职责归属。
3. 明确 MySQL 中保存的业务数据。
4. 明确当前 Java 直连 OpenAI 的生成链路。
5. 明确 Python AI 服务是未来扩展项，不是当前 MVP 必需项。

后续建议把 `requirement.txt` 中的后端技术方案更新为：

```text
Vue 前端 + Java Spring Boot 主后端 + MySQL + OpenAI 图片生成接口
```

而不是 Node.js / NestJS / FastAPI 单后端方案，也不是默认依赖 Python FastAPI AI 服务的三层方案。
