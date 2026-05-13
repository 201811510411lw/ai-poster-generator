# AGENTS.md

本文件是 Codex 在当前仓库工作的项目级指令。执行任何代码修改、配置调整、文档更新或排查任务前，必须优先阅读并遵守本文件。

## 项目背景

本项目是 AI Poster Generator，一个 AI 海报生成 Web 应用 monorepo。

当前 MVP 架构：

- `frontend/`：Vue 3 + Vite + TypeScript 前端。
- `backend-java/`：Spring Boot 后端，负责认证、素材上传、海报生成任务、Prompt 构造、OpenAI 图片接口调用、图片存储。
- `docs/`：产品、架构和设计文档。
- 当前阶段不需要独立 Python AI 服务；只有在接入 ComfyUI、Stable Diffusion/Flux、本地模型、高级图片后处理或多模型网关时，才考虑新增 Python 服务。

## 总体工作原则

- 修改前先阅读相关代码、README、配置和调用链。
- 优先做最小变更，不做无关重构。
- 不要自动格式化整个项目。
- 不要修改和当前任务无关的文件。
- 不要删除用户已有代码、注释、配置或未提交改动。
- 不要引入不必要的新依赖。
- 不要把真实密钥、token、密码、AK/SK、数据库连接串写入代码、文档或示例配置。
- 涉及 API、数据库、认证、图片生成、存储、CORS 的变更，必须说明影响范围和验证方式。

## 仓库结构

```text
ai-poster-generator/
  frontend/          # Vue 3 + Vite + TypeScript 前端
  backend-java/      # Spring Boot 后端
  docs/              # 产品和架构文档
  docker-compose.yml # 本地编排，若存在则用于本地联调
```

## 前端规则

前端目录：

```bash
frontend
```

常用命令：

```bash
cd frontend
pnpm install
pnpm run dev
pnpm run typecheck
pnpm run build
```

修改前端时遵守：

- 使用 Vue 3 Composition API 和 TypeScript。
- 保持现有路由结构，不要随意重命名已有路由。
- 保持现有 API 调用方式和认证流程。
- 修改接口字段时，必须同步检查后端 DTO、前端类型、页面调用和错误处理。
- UI 调整要保持清晰、克制，不要引入复杂动效或无关视觉重构。
- 不要直接把后端地址写死到业务代码；优先使用 `VITE_API_BASE_URL`。
- 修改完成后优先运行：

```bash
cd frontend
pnpm run typecheck
pnpm run build
```

如果无法执行，最终回复中必须说明原因。

## 后端规则

后端目录：

```bash
backend-java
```

技术栈：

- Spring Boot 3.5.x
- Java 21
- Maven
- MySQL
- JPA
- JWT
- OpenAI Image API
- 本地存储 / 火山引擎 TOS

常用命令：

```bash
cd backend-java
mvn test
mvn package
mvn spring-boot:run
```

修改后端时遵守：

- 保持 Controller、Service、Repository、DTO 的职责边界清晰。
- 不要在 Controller 中堆业务逻辑。
- 不要把 OpenAI、TOS、JWT、数据库等密钥写死在代码里。
- 新增配置必须走环境变量或 `application.yml` 占位符。
- 涉及数据库字段、实体、初始化 SQL 的修改，必须同步检查：
  - Entity
  - Repository
  - Service
  - DTO
  - `src/main/resources/db/init.sql`
  - README 或相关文档
- 涉及认证、JWT、CORS、上传、文件访问、图片生成接口的修改，必须说明安全影响。
- 修改完成后优先运行：

```bash
cd backend-java
mvn test
mvn package
```

如果无法执行，最终回复中必须说明原因。

## 配置和密钥规则

本项目涉及以下敏感配置：

- `OPENAI_API_KEY`
- `JWT_SECRET`
- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `TOS_ACCESS_KEY`
- `TOS_SECRET_KEY`
- `TOS_BUCKET`
- `CORS_ALLOWED_ORIGINS`

规则：

- 不要提交真实 `.env` 文件。
- 不要把真实密钥写入 README、代码、测试、SQL、YAML。
- 示例配置只能使用占位符，例如 `your_api_key`、`change-me`。
- 修改 `application.yml` 时，不要把默认值改成生产真实值。
- 修改 CORS 时，不要为了省事设置为无限开放，除非用户明确要求并说明风险。
- 修改 JWT 逻辑时，必须注意 token 过期、签名密钥、认证失败返回和前端登录态处理。

## OpenAI 图片生成规则

当前后端直接调用 OpenAI 图片生成接口。

修改相关逻辑时：

- 先阅读 Prompt 构造逻辑、OpenAI Client、Poster Service、任务记录和存储逻辑。
- 不要把用户上传素材误认为已经作为真实图片输入传给模型；如果只是写入 Prompt，需要明确说明限制。
- 如果要支持参考图、商品图、Logo 保真生成，应明确这是功能升级，可能需要改造接口、请求格式、存储结构和前端交互。
- 不要在日志中输出完整 Prompt、密钥、base64 图片内容或用户敏感素材。
- 图片生成失败时，要保留清晰错误信息，但不要泄露密钥或内部堆栈给前端。

## 存储规则

项目支持本地存储和 TOS 对象存储。

修改存储逻辑时：

- 保持本地存储和对象存储抽象清晰。
- 不要把存储路径、bucket、endpoint、AK/SK 写死。
- 删除素材或生成图时，必须说明是否只删除元数据，还是同时删除物理文件。
- 涉及文件删除、覆盖、迁移时，必须先说明风险和回滚方式。

## 数据库规则

涉及 MySQL、JPA、初始化 SQL 时：

- 不要直接写破坏性 SQL。
- 不要默认执行 `drop table`、`truncate table`、无条件 `delete from`。
- 修改表结构时，必须说明兼容性影响。
- 修改初始化 SQL 时，必须同步检查实体类和接口返回。
- 查询接口要注意分页、排序、用户隔离和权限边界。

## 搜索和阅读代码

查找文件优先使用：

```bash
rg --files
```

查找内容优先使用：

```bash
rg "关键词"
```

修改前应根据任务阅读相关调用链，不要只看单个文件。

例如：

- 修改登录：同时检查前端登录页、API 客户端、后端 Auth Controller、JWT 逻辑、安全配置。
- 修改海报生成：同时检查前端生成页面、Poster API、Poster Service、Prompt 构造、OpenAI Client、Storage Service。
- 修改素材上传：同时检查前端上传组件、后端上传接口、Asset Service、存储实现和数据库实体。

## Git 规则

不要自动提交代码，除非用户明确要求。

不要执行破坏性 Git 命令，例如：

```bash
git reset --hard
git clean -fd
git checkout -- .
```

如果发现用户已有未提交改动，先说明，不要覆盖。

生成 commit message 时使用中文 Conventional Commit 风格：

```text
feat: 新增 GPT-Image2 结构化海报提示词模板
fix: 修复素材 ID 类型不一致问题
refactor: 重构海报提示词构造逻辑
docs: 更新本地启动说明
chore: 调整前端构建配置
test: 补充海报生成接口测试
```

格式要求：

- 类型使用英文小写，如 `feat`、`fix`、`refactor`、`docs`、`style`、`chore`、`test`。
- 冒号后使用中文说明。
- 中文说明采用“动词 + 对象 + 补充说明”的短句。
- 不使用英文长句。

## PR / 变更说明规则

如果用户要求生成 PR 描述，使用以下格式：

```md
## 变更内容

- 

## 验证方式

- 

## 风险点

- 

## 回滚方式

- 
```

如果涉及接口、数据库、认证、存储或图片生成，必须补充兼容性说明。

## 验证要求

根据修改类型选择验证：

### 前端

```bash
cd frontend
pnpm run typecheck
pnpm run build
```

### 后端

```bash
cd backend-java
mvn test
mvn package
```

### 文档

- 检查命令、路径、端口、环境变量名称是否和项目一致。
- 不要把真实密钥写入文档。

### 配置

- 检查 YAML 缩进。
- 检查环境变量名是否和代码读取逻辑一致。
- 检查默认值是否适合本地开发，不能误导生产使用。

## 最终回复要求

最终回复保持简洁，包含：

1. 修改了什么。
2. 验证了什么。
3. 哪些内容未验证。
4. 是否存在风险。
5. 如果涉及生产、密钥、数据库、存储或认证，说明是否需要人工确认。
