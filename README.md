# Enterprise Feature Flag & A/B Testing Engine

## PROJECT MISSION
This engine aims to provide sub-10ms feature flag evaluation latency through local cache evaluation and real-time updates via Server-Sent Events (SSE). Designed for high-throughput, low-latency enterprise applications requiring reliable feature toggling and experimentation capabilities.

## SYSTEM ARCHITECTURE
- **PostgreSQL**: Serves as the source of truth for flag definitions, targeting rules (stored as JSONB), and experiment configurations.
- **Redis**: Provides distributed caching for flag evaluation results and Pub/Sub mechanism for instance synchronization across backend nodes.
- **Spring WebFlux/SseEmitter**: Enables real-time push of flag updates to connected clients using reactive streaming and Server-Sent Events.

## MONOREPO STRUCTURE
```
enterprise-flag-engine/
├── backend/                 # Spring Boot 21/Java 21 backend service
├── frontend/                # Next.js 13+ App Router frontend application
└── docker-compose.yml       # Local development infrastructure (PostgreSQL, Redis)
```

## LOCAL SETUP QUICKSTART
1. Start infrastructure services:
   ```bash
   docker-compose up -d
   ```

2. Build and run the backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. Build and run the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

The backend will be available at `http://localhost:8080` and the frontend at `http://localhost:3000`.

## TEAM WORKFLOW CONTRACT
- **Trunk-Based Development**: All development occurs directly on the `main` branch. Short-lived feature branches are prohibited; instead, we use feature flags for incomplete work.
- **Conventional Commits**: All commit messages must follow the Conventional Commits specification (e.g., `feat: add user dashboard`, `fix: resolve cache race condition`).
- **Contract-First Collaboration**: Backend and frontend teams collaborate via explicit API contracts (OpenAPI/Swagger) before implementation. Backend provides API stubs; frontend develops against mocked endpoints until the contract is finalized.

---
*Engineered for performance, reliability, and developer velocity.*