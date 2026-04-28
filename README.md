# AI Poster Generator

AI Poster Generator is being organized as a monorepo.

## Project structure

```text
ai-poster-generator/
  frontend/          # Vue 3 + Vite + TypeScript web app
  backend-java/      # Spring Boot backend (planned)
  ai-service-python/ # FastAPI AI service (planned)
  docs/              # Product and architecture documents
  docker-compose.yml # Local full-stack orchestration (planned)
```

## Frontend

The current Vue application lives in `frontend/`.

```bash
cd frontend
npm install
npm run dev
npm run typecheck
npm run build
```

Current frontend route:

```text
/poster-generator
```

This branch only moves the frontend project into `frontend/`; backend, Python AI service, and Docker orchestration will be added in later branches.
