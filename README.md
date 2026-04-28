# AI Poster Generator

AI Poster Generator is organized as a monorepo for the Vue frontend, Spring Boot backend, and future Python AI service.

## Project structure

```text
ai-poster-generator/
  frontend/          # Vue 3 + Vite + TypeScript web app
  backend-java/      # Spring Boot backend
  ai-service-python/ # FastAPI AI service (planned)
  docs/              # Product and architecture documents
  docker-compose.yml # Local full-stack orchestration (planned)
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

Frontend auth can run in mock mode by default. To connect to the Java backend, create `frontend/.env` from `frontend/.env.example` and set:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK_AUTH=false
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
