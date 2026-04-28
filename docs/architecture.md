# AI 平面物料生成系统架构设计

本文档基于 `requirement.txt` 的产品需求，结合 SeaTunnel Web 这类企业级 Web 管理平台的工程组织方式，明确本项目后续采用的前端、Java 后端、Python AI 服务和数据库的整体架构。

## 1. 架构目标

本系统第一阶段目标是跑通以下核心闭环：

```text
用户登录
  ↓
上传素材
  ↓
填写颜色、文案、物料类型、尺寸和设计要求
  ↓
创建 AI 生成任务
  ↓
调用图像生成模型
  ↓
保存结果
  ↓
前端预览和下载
```

系统不是桌面端软件，也不是 ComfyUI 节点式工具，而是一个面向电脑端浏览器使用的 AI 平面物料生成 Web 系统。

## 2. 总体技术架构

推荐采用前端、Java 主后端、Python AI 服务三层结构：

```text
Vue 前端
  ↓ HTTP / JSON / multipart
Java Spring Boot 主后端
  ↓ HTTP / JSON
Python FastAPI AI 服务
  ↓ SDK / HTTP
OpenAI / Grok / ComfyUI / 本地模型
```

其中：

- Vue 负责页面展示和用户交互。
- Java 负责登录、用户、权限、素材、任务、数据库、文件存储等主业务。
- Python 负责 AI 图像生成、图片处理、多模型适配和后续本地模型扩展。
- MySQL 保存用户、素材、任务、生成结果等业务数据。
- 本地文件系统、MinIO、OSS 或 S3 保存上传素材和生成图片。

## 3. 推荐目录结构

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
      task/
      ai/
      storage/
    src/main/resources/
      application.yml
      application-dev.yml

  frontend/
    package.json
    src/
      api/
      router/
      stores/
      layouts/
      views/
        login/
        poster-generator/
        tasks/
        assets/

  ai-service-python/
    app/
      main.py
      api/
      providers/
      schemas/
      services/
      utils/
    requirements.txt

  docs/
    architecture.md
    api.md
    database.md
```

当前仓库已经有 Vue 前端代码，后续可以逐步迁移到 `frontend/` 目录，或者暂时保持现有结构，等 Java 后端和 Python AI 服务加入后再整理为 monorepo。

## 4. 各层职责边界

### 4.1 Vue 前端

前端只负责页面展示、表单交互和调用 Java 后端接口，不直接访问 Python AI 服务，也不直接访问 OpenAI、Grok 或本地模型。

主要职责：

1. 登录页。
2. 主工作台布局。
3. 素材上传区。
4. 基础配置区。
5. 生成参数区。
6. 结果预览区。
7. 任务历史页。
8. 素材管理页。
9. token 保存和请求拦截。
10. 401 跳转登录页。

推荐前端模块：

```text
src/api/request.ts        # Axios 实例、token、错误处理
src/api/auth.ts           # 登录、当前用户、退出登录
src/api/assets.ts         # 素材上传、删除、查询
src/api/poster.ts         # 创建生成任务
src/api/tasks.ts          # 查询任务状态、历史记录、下载结果
src/router/index.ts       # 路由和登录守卫
src/stores/auth.ts        # 登录用户状态
src/stores/poster.ts      # 海报生成表单状态
src/layouts/AppShell.vue  # 系统主布局
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
8. 物料类型和尺寸规则管理。
9. 调用 Python AI 服务。
10. 生成结果保存。
11. 任务状态更新。
12. 历史记录查询。
13. 图片下载。
14. 统一异常处理和接口返回。

推荐 Java 包结构：

```text
common/
  ApiResponse.java
  ErrorCode.java
  BusinessException.java
  GlobalExceptionHandler.java

config/
  WebConfig.java
  CorsConfig.java
  JacksonConfig.java

security/
  JwtTokenProvider.java
  JwtAuthenticationFilter.java
  SecurityConfig.java
  LoginUserContext.java

auth/
  AuthController.java
  AuthService.java
  dto/LoginRequest.java
  dto/LoginResponse.java
  dto/CurrentUserResponse.java

user/
  UserController.java
  UserService.java
  UserEntity.java
  UserMapper.java

asset/
  AssetController.java
  AssetService.java
  AssetEntity.java
  AssetMapper.java
  dto/AssetUploadResponse.java

poster/
  PosterController.java
  PosterService.java
  PromptBuilder.java
  MaterialRuleService.java
  dto/GeneratePosterRequest.java
  dto/GeneratePosterResponse.java

task/
  PosterTaskController.java
  PosterTaskService.java
  PosterTaskEntity.java
  PosterTaskMapper.java

ai/
  AiImageClient.java
  dto/AiGenerateRequest.java
  dto/AiGenerateResponse.java

storage/
  StorageService.java
  LocalStorageService.java
  MinioStorageService.java
  StoredFile.java
```

### 4.3 Python FastAPI AI 服务

Python AI 服务只负责 AI 能力和图片处理，不直接管理用户、权限、业务任务和数据库主数据。

主要职责：

1. 接收 Java 后端传入的生成参数。
2. 接收素材 URL 或素材文件引用。
3. 组装或增强模型 prompt。
4. 调用 OpenAI 图像模型。
5. 调用 Grok 图像模型。
6. 后续接入 ComfyUI、Flux、Stable Diffusion 或本地模型。
7. 图片压缩、格式转换、尺寸处理。
8. 返回图片 base64、临时文件路径或结果 URL 给 Java。

推荐 Python 结构：

```text
app/main.py
app/api/image.py
app/schemas/image.py
app/services/prompt_service.py
app/services/image_service.py
app/providers/base.py
app/providers/openai_provider.py
app/providers/grok_provider.py
app/providers/comfyui_provider.py
app/providers/mock_provider.py
app/utils/image_io.py
app/utils/http_client.py
```

Python 暴露给 Java 的内部接口：

```http
POST /ai/generate-image
POST /ai/edit-image
GET  /health
```

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

### 5.3 用户表建议

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

对应需求文档中的素材上传区，素材由 Java 后端接收并保存。

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

### 6.3 素材表建议

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
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 7. 海报生成任务设计

生成任务由 Java 创建和管理，Python 只负责执行 AI 生成。

### 7.1 推荐任务流程

```text
Vue 点击生成
  ↓
POST /api/poster/generate
  ↓
Java 校验参数
  ↓
Java 创建 poster_task，状态 PENDING
  ↓
Java 组装业务 prompt 和素材 URL
  ↓
Java 调用 Python /ai/generate-image
  ↓
Python 调用 OpenAI/Grok/本地模型
  ↓
Python 返回 image_base64 或临时结果
  ↓
Java 保存生成图片
  ↓
Java 更新 poster_task 为 SUCCESS 或 FAILED
  ↓
Vue 查询任务状态并展示结果
```

第一期为了降低复杂度，可以先做同步生成：`POST /api/poster/generate` 等待 Python 返回结果后直接返回。

第二期再改成异步任务：创建任务后前端轮询 `GET /api/poster/tasks/{taskId}`。

### 7.2 Java 对前端接口

```http
POST /api/poster/generate
GET  /api/poster/tasks/{taskId}
GET  /api/poster/tasks
GET  /api/poster/tasks/{taskId}/download
```

### 7.3 Java 调 Python 接口

```http
POST /ai/generate-image
```

请求示例：

```json
{
  "provider": "openai",
  "model": "gpt-image-1.5",
  "materialType": "poster",
  "width": 1080,
  "height": 1920,
  "prompt": "请生成一张竖版宣传海报...",
  "assetUrls": [
    "http://localhost:8080/files/assets/1.png",
    "http://localhost:8080/files/assets/2.png"
  ],
  "outputFormat": "png"
}
```

返回示例：

```json
{
  "success": true,
  "provider": "openai",
  "model": "gpt-image-1.5",
  "mimeType": "image/png",
  "imageBase64": "...",
  "usage": {
    "inputTokens": 0,
    "outputTokens": 0
  }
}
```

### 7.4 任务表建议

```sql
CREATE TABLE poster_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  material_type VARCHAR(32) NOT NULL,
  width INT NOT NULL,
  height INT NOT NULL,
  main_color VARCHAR(32),
  sub_color VARCHAR(32),
  brand_description TEXT,
  style_description TEXT,
  title VARCHAR(255),
  subtitle VARCHAR(255),
  activity_description TEXT,
  design_requirement TEXT,
  output_format VARCHAR(16) DEFAULT 'png',
  asset_ids JSON,
  prompt TEXT,
  provider VARCHAR(32),
  model VARCHAR(64),
  status VARCHAR(32) NOT NULL,
  result_image_url VARCHAR(512),
  result_storage_path VARCHAR(512),
  error_message TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

状态建议：

```text
PENDING
GENERATING
SUCCESS
FAILED
CANCELED
```

## 8. Prompt 组装职责

Prompt 分两层：

1. Java 负责根据业务参数生成结构化业务 prompt。
2. Python 可以根据不同模型做二次适配。

Java 生成基础 prompt：

```text
请生成一张竖版宣传海报，画面比例为 1080:1920。
整体风格年轻、活力、促销感强。
主色使用 #E60012，辅助色使用 #FFD700。
使用上传的产品图作为画面主体，保持产品外观自然清晰。
标题文字为“夏日焕新季”，副标题为“全场低至7折”。
活动说明为“4月23日-5月10日”。
构图要求：产品放中间，标题在上方，底部保留活动信息。
画面需要清晰、干净、有设计感，适合门店宣传使用。
避免杂乱排版、错误文字、模糊图片、低清晰度和不相关元素。
```

Python 根据 provider 做模型适配，例如：

- OpenAI provider：适配图片输入、输出格式、尺寸参数。
- Grok provider：适配接口参数和多图合成方式。
- ComfyUI provider：适配工作流 JSON。
- Mock provider：开发阶段返回示例图。

## 9. 配置设计

### 9.1 Java application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: ai-poster-generator
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/ai_poster_generator?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 200MB

jwt:
  secret: ${JWT_SECRET:change-me-in-production}
  expiration-seconds: 604800

storage:
  type: local
  local:
    base-path: ${STORAGE_BASE_PATH:./data/uploads}
    public-base-url: ${STORAGE_PUBLIC_BASE_URL:http://localhost:8080/files}

ai-service:
  base-url: ${AI_SERVICE_BASE_URL:http://localhost:9000}
  timeout-seconds: 120
```

### 9.2 Python .env

```env
AI_PROVIDER=mock
OPENAI_API_KEY=
XAI_API_KEY=
OUTPUT_DIR=./data/results
```

密钥不要提交到 GitHub。仓库中只保留 `.env.example`。

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

### 第 4 阶段：Python AI 服务

1. FastAPI 项目初始化。
2. `/health`。
3. `/ai/generate-image`。
4. Mock provider。
5. OpenAI provider。
6. Grok provider。

### 第 5 阶段：生成任务闭环

1. Java 创建任务。
2. Java 组装 prompt。
3. Java 调 Python。
4. Python 返回图片。
5. Java 保存结果。
6. 前端展示和下载。

## 11. 与 requirement.txt 的关系

`requirement.txt` 主要描述产品需求、页面结构、字段、接口初稿和验收标准。

本文档补充系统工程设计，主要解决：

1. 采用 Vue + Java + Python 的最终技术路线。
2. 明确登录、用户、素材、任务、AI 服务的职责归属。
3. 明确 MySQL 中需要保存哪些业务数据。
4. 明确 Java 和 Python 如何协作。
5. 明确 MVP 阶段开发顺序。

后续建议把 `requirement.txt` 中的后端技术方案更新为：

```text
Java Spring Boot 主后端 + MySQL + Python FastAPI AI 服务
```

而不是原先的 Node.js / NestJS / FastAPI 单后端方案。
