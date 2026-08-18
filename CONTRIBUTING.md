# Contributing to LPMS

Thanks for your interest in contributing to the Legal Practice Management System!
This guide covers how to set up a development environment and submit changes.

## Code of Conduct

Be respectful and constructive. Harassment or personal attacks are not tolerated.
When reviewing or commenting on work, focus on the code and the problem.

## Getting Started

### Prerequisites

- **Docker** & Docker Compose v2.0+ (recommended — runs the whole stack)
- **JDK 17+** (for backend work outside Docker)
- **Node.js 18+** (for frontend work outside Docker)
- **Maven** is bundled via the `./mvnw` wrapper in `backend/`

### Run the full stack with Docker

```bash
cp .env.example .env          # optional: tweak values
docker compose up -d          # builds + starts backend, frontend, MySQL, Redis, ...
```

Once running:

| Service        | URL                                    |
|----------------|----------------------------------------|
| Frontend       | http://localhost:5174                  |
| Backend API    | http://localhost:9874/api              |
| Swagger UI     | http://localhost:9874/api/swagger-ui.html |
| Health check   | http://localhost:9874/api/actuator/health |

### Demo login credentials

A demo law firm and accounts are seeded automatically on first boot (see
`backend/src/main/resources/db/migration/V13__seed_demo_firm_and_users.sql`):

| Email                  | Password    | Role               |
|------------------------|-------------|--------------------|
| `admin@lawfirm.com`    | `Admin123!` | Firm Administrator |
| `lawyer@lawfirm.com`   | `Lawyer123!`| Lawyer             |

> ⚠️ These are demo credentials for local development only. Change them (and the
> JWT secret) before any real deployment — see the "Security" section of the README.

### Local development (without Docker for the app)

You can run the backend and frontend directly while keeping the infrastructure
services in Docker:

```bash
# 1. Start just the infrastructure
docker compose up -d mysql redis

# 2. Backend (uses the dev profile, which talks to the published MySQL/Redis ports)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Frontend
cd frontend
npm install
npm run dev
```

## Development Workflow

1. **Fork & branch.** Create a branch off `main`:
   `git checkout -b feat/short-description`.
2. **Make your changes.** Keep commits focused; write a clear commit message.
3. **Test locally.**
   ```bash
   # Backend
   cd backend && ./mvnw clean test

   # Frontend (lint + build)
   cd frontend && npm run lint && npm run build
   ```
4. **Open a Pull Request** against `main`. Reference any related issue and
   describe **what** changed and **why**.

### Branch & commit conventions

- Branch prefixes: `feat/`, `fix/`, `docs/`, `chore/`, `refactor/`.
- Write commit messages in the imperative mood: "Add client export endpoint",
  not "Added client export endpoint".

## Code Style

### Backend (Java / Spring Boot)

- Java 17 features are welcome (records, switch expressions, text blocks).
- Follow the existing package layout under `com.company.legal.*`:
  `controller`, `service`, `repository`, `entity`, `dto`.
- Use Lombok where the surrounding code does; match existing patterns.
- New REST endpoints go under `/v1/...` and return the project's
  `ApiResponse` / `PageResponse` envelopes.
- Don't change database columns without a new Flyway migration
  (`V<n+1>__description.sql`). Never edit an already-applied migration —
  add a new one instead.

### Frontend (React / TypeScript)

- TypeScript strict; no `any` unless unavoidable (and then comment why).
- Use the existing `apiGet` / `apiPost` / `apiPut` / `apiDelete` helpers from
  `src/services/api.ts` for all HTTP calls.
- Match the existing Tailwind utility classes and component patterns
  (`.card`, `.btn-primary`, `.input`, `.badge-*`, the `Modal` component).

## Database Migrations

Migrations live in `backend/src/main/resources/db/migration/` and run
automatically on startup via Flyway.

- **Always add a new migration** — never modify one that has already been
  applied to any shared environment (Flyway checksums will reject it).
- Make migrations **idempotent** where possible (`ON DUPLICATE KEY UPDATE`,
  guarded `INSERT ... WHERE NOT EXISTS`) so they're safe to re-run.
- If you add an entity that extends `BaseEntity`, make sure its table has the
  audit columns (`created_by`, `updated_by`, `deleted_at`, `version`) — see the
  "Backend gotchas" note in the README.

## Reporting Bugs & Requesting Features

Open a GitHub Issue and include:

- **What happened** (steps to reproduce, expected vs. actual behavior).
- **Environment** (OS, Docker version, browser if frontend-related).
- **Logs** — paste the relevant `docker compose logs <service>` output.
  **Never paste real secrets, JWTs, or password hashes.**

## Security Vulnerabilities

Found a security issue? **Please do not open a public issue.** See the
"Security" section of the README for how to report it privately.

## License

By contributing, you agree that your contributions are licensed under the
project's [MIT License](./LICENSE).
