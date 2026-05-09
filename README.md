# AI Poster Generator

AI Poster Generator is a monorepo for an AI poster generation web app. The current MVP architecture uses a Vue frontend and a Spring Boot backend. The Java backend handles authentication, asset upload, poster generation tasks, local/object storage, and direct OpenAI image generation.

## Project structure

```text
ai-poster-generator/
  frontend/          # Vue 3 + Vite + TypeScript web app
  backend-java/      # Spring Boot backend, business APIs, storage, OpenAI image generation
  docs/              # Product and architecture documents
  docker-compose.yml # Local full-stack orchestration (planned)
```

`ai-service-python` is not required for the current MVP. A separate Python AI service can be added later only if the project needs ComfyUI, Stable Diffusion/Flux local models, advanced image post-processing, or a multi-model gateway.

## Current architecture

```text
Vue frontend
  ↓ HTTP / JSON / multipart
Java Spring Boot backend
  ├─ Auth / JWT
  ├─ Asset upload and metadata
  ├─ Poster generation task records
  ├─ Prompt building
  ├─ Direct OpenAI image API call
  └─ Generated image storage
```

The current poster generation path is:

```text
POST /api/posters/generate
  -> PosterService builds the prompt
  -> OpenAiImageClient calls /v1/images/generations
  -> Java decodes returned base64 image data
  -> StorageService stores the generated image
  -> Java returns taskId, status, imageUrl, width, and height
```

## Frontend

```bash
cd frontend
pnpm install
pnpm run dev
pnpm run typecheck
pnpm run build
```

Routes:

```text
/login
/poster-generator
```

Frontend authentication uses the Java backend. Create `frontend/.env` from `frontend/.env.example`:

```bash
cp frontend/.env.example frontend/.env
```

Default local backend URL:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Java backend

```bash
cd backend-java
mysql -uroot -p < src/main/resources/db/init.sql
mvn spring-boot:run
```

Default local backend URL:

```text
http://localhost:8080
```

Default test account:

```text
username: admin
password: admin123
```

Required image generation configuration:

```env
OPENAI_API_KEY=your_api_key
OPENAI_BASE_URL=https://api.openai.com
OPENAI_IMAGE_MODEL=gpt-image-1
OPENAI_IMAGE_SIZE=auto
OPENAI_IMAGE_QUALITY=medium
```

## Main backend APIs

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

## Current limitation

Uploaded assets are stored and associated with poster generation tasks. At the moment, selected assets are summarized into the prompt as references by type, filename, and image size. They are not yet sent to the model as actual image inputs. If generated posters must faithfully use uploaded product images or logos, the next step is to extend the OpenAI client to support image-edit/reference-image generation instead of text-only generation.
