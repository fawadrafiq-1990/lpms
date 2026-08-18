# Legal Practice Management System (LPMS)

**Enterprise-grade AI-powered legal practice management system with multi-tenant architecture**

[![CI](https://github.com/fawadrafiq-1990/lpms/actions/workflows/ci.yml/badge.svg)](https://github.com/fawadrafiq-1990/lpms/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

## 🚀 Quick Start


### Prerequisites
- Docker & Docker Compose v2.0+
- JDK 17+
- Node.js 18+
- kubectl (for Kubernetes deployment)

### Development Setup

```bash
# 1. Clone and setup
git clone https://github.com/fawadrafiq-1990/lpms.git
cd lpms
cp .env.example .env

# 2. Start all services (backend, frontend, MySQL, Redis, Kafka, Elasticsearch, ...)
docker compose up -d

# 3. Access the application (the backend takes ~30-60s to boot on first run)
# Frontend:       http://localhost:5174
# Backend API:    http://localhost:9874/api
# Swagger UI:     http://localhost:9874/api/swagger-ui.html
# Health check:   http://localhost:9874/api/actuator/health
```

#### Demo login

A demo law firm and accounts are seeded automatically on first boot:

| Email                  | Password    | Role               |
|------------------------|-------------|--------------------|
| `admin@lawfirm.com`    | `Admin123!` | Firm Administrator |
| `lawyer@lawfirm.com`   | `Lawyer123!`| Lawyer             |

> ⚠️ These are demo credentials for local development only. Change them and the
> JWT secret before any real deployment (see [Security](#-security)).

### Production Deployment

```bash
# Option 1: Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# Option 2: Kubernetes
kubectl apply -f k8s/
kubectl rollout status deployment/lpms-backend
kubectl rollout status deployment/lpms-frontend
```

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Configuration](#configuration)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Security](#security)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)

## ✨ Features

### Core Features
- **Multi-Tenant Architecture** - Complete law firm data isolation
- **Case Management** - Full lifecycle from conflict check to closure
- **Client Management** - Individual, corporate, government, and organization support
- **Document Management** - Versioned storage with access control
- **Hearing & Court Management** - Calendar integration with reminders
- **Task & Deadline Management** - Dependency tracking with escalation
- **Billing & Invoicing** - Time tracking, invoice generation, payment processing
- **Conflict of Interest Checking** - Automated conflict detection
- **Contract Management** - Engagement letters and fee agreements

### AI Features
- **AI Legal Assistant** - RAG-powered legal research
- **Document Analysis** - AI-powered document review
- **Case Summarization** - Automatic case summaries
- **Multi-Agent System** - Specialized agents for different tasks

### Communication
- **Internal Messaging** - Team communication
- **Client Portal** - Secure client access
- **Multi-Channel Notifications** - In-app, email, SMS
- **WebSocket Integration** - Real-time updates

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Load Balancer                         │
│                      (nginx/ingress)                         │
└───────────────────────────┬─────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐  ┌───────▼────────┐
│   Frontend     │  │   Backend    │  │   Static       │
│   (React)      │  │  (Spring)    │  │   Assets       │
│   Container    │  │  Container   │  │   (CDN)        │
└────────────────┘  └──────┬───────┘  └────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌─────▼──────┐  ┌───────▼────────┐
│     MySQL      │  │   Redis    │  │     Kafka      │
│   (Primary)    │  │   (Cache)  │  │   (Events)     │
└────────────────┘  └────────────┘  └────────────────┘
```

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Language |
| Spring Boot | 3.3 | Framework |
| Spring Security | 6.3 | Auth & Security |
| Spring Data JPA | 3.3 | Database ORM |
| MySQL | 8.0 | Primary Database |
| Redis | 7 | Caching & Sessions |
| Apache Kafka | 3.6 | Event Streaming |
| Elasticsearch | 8.11 | Search |
| Flyway | 10.10 | Migrations |
| Ollama | latest | Local LLM |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18 | UI Framework |
| TypeScript | 5 | Type Safety |
| Vite | 5 | Build Tool |
| Tailwind CSS | 3 | Styling |
| Zustand | 5 | Client State |
| React Router | 6 | Routing |
| Axios | 1 | HTTP Client |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Kubernetes | Orchestration |
| Prometheus | Metrics |
| Grafana | Visualization |
| ELK Stack | Logging |
| GitHub Actions | CI/CD |

## 🔧 Configuration

### Environment Variables

Create a `.env` file based on `.env.example`:

```bash
# Database
MYSQL_ROOT_PASSWORD=your_secure_password_here
MYSQL_DATABASE=lpm_production
MYSQL_USER=lpm_user
MYSQL_PASSWORD=your_user_password

# JWT Security
JWT_SECRET=your_256_bit_minimum_secret_key_here!
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Production Checklist

- [ ] Change all default passwords
- [ ] Generate secure JWT_SECRET (256-bit minimum)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS for specific origins
- [ ] Set up database backups
- [ ] Configure monitoring and alerting
- [ ] Enable rate limiting
- [ ] Set up log aggregation
- [ ] Configure secret management (Vault/AWS Secrets Manager)

## 🧪 Testing

```bash
# Backend Unit & Integration Tests
cd backend
./mvnw clean test

# Backend with coverage
./mvnw clean test jacoco:report

# Frontend Tests
cd frontend
npm test

# Frontend with coverage
npm run test:coverage

# Run specific test
./mvnw test -Dtest=AuthServiceTest

# Integration tests
./mvnw verify -Pintegration-test
```

### Test Coverage Requirements

- **Minimum Coverage**: 80%
- **Critical Path**: 90%+ (Auth, Billing, Cases)
- Run coverage report: `./mvnw jacoco:report`

## 📊 Monitoring

### Health Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Liveness probe |
| `/actuator/health/ready` | Readiness probe |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus format metrics |

### Grafana Dashboards

Access dashboards at `http://localhost:3000` with default credentials `admin/admin`.

Key dashboards:
- **Application Overview** - Overall system health
- **JVM Metrics** - Memory, CPU, threads
- **Database Metrics** - Connection pool, query times
- **Business Metrics** - Cases, clients, invoices

### Alerting

Critical alerts configured for:
- High error rate (>1%)
- High latency (p95 > 2s)
- Database connection pool exhaustion
- Disk space < 20%
- Service down

## 🔐 Security

### Implemented Security Measures

| Measure | Status |
|---------|--------|
| JWT Authentication | ✅ |
| BCrypt Password Hashing (cost 12) | ✅ |
| CORS Configuration | ✅ |
| Rate Limiting (Redis) | ✅ |
| SQL Injection Prevention | ✅ |
| XSS Protection | ✅ |
| CSRF Protection | ✅ |
| Input Validation | ✅ |
| Secure Headers | ✅ |
| HTTPS/TLS | ⚠️ Configure in production |

### Security Scanning

```bash
# Run dependency vulnerability scan
./mvnw dependency:check

# Run OWASP ZAP scan (requires Docker)
docker run -v $(pwd):/zap/wrk/:rw \
  owasp/zap2docker-stable zap-baseline.py \
  -t https://localhost:8080
```

### Secrets Management

**Never commit secrets!** Use:
- HashiCorp Vault (recommended)
- AWS Secrets Manager
- Azure Key Vault
- Kubernetes Secrets (encrypted at rest)

## 📚 API Documentation

### Swagger UI
Available at `http://localhost:9874/api/swagger-ui.html` once the backend is running.

### OpenAPI Spec
Available at `http://localhost:9874/api/v3/api-docs`.

### Key Endpoints

All endpoints are prefixed with `/api` (the backend context path). The `/v1/...`
paths below are relative to that prefix.

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /v1/auth/login | User login | No |
| POST | /v1/auth/register | User registration | No |
| POST | /v1/auth/refresh | Refresh token | Yes |
| GET | /v1/cases | List cases | Yes |
| POST | /v1/cases | Create case | Yes |
| GET | /v1/clients | List clients | Yes |
| POST | /v1/clients | Create client | Yes |
| POST | /v1/invoices | Create invoice | Yes |
| GET | /v1/calendar/events | List calendar events | Yes |
| GET | /v1/hearings | List hearings | Yes |
| GET | /v1/tasks | List tasks | Yes |

## 📖 Documentation

- [Deployment Guide](DEPLOYMENT.md) - Production deployment instructions
- [Contributing](CONTRIBUTING.md) - How to set up a dev environment and contribute
- [API Reference](#-api-documentation) - Interactive Swagger UI (see above)

## 🐳 Docker

### Development

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Restart specific service
docker-compose restart backend
```

### Production

```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.prod.yml up -d

# Scale backend
docker-compose -f docker-compose.prod.yml up -d --scale backend=3
```

## 🚨 Troubleshooting

### Common Issues

#### Database Connection Failed
```bash
# Check MySQL is running
docker-compose ps mysql

# Check logs
docker-compose logs mysql

# Verify credentials in .env
grep MYSQL .env
```

#### Backend Won't Start
```bash
# Check Java version
java -version  # Should be 17+

# Check logs
docker-compose logs backend

# Run migrations manually
docker-compose exec backend ./mvnw flyway:migrate
```

#### Frontend Won't Build
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Check Node version
node -v  # Should be 18+
```

#### High Memory Usage
```bash
# Check container resources
docker stats

# Increase limits in docker-compose.yml
# or scale horizontally
```

### Getting Help

- **Logs**: `docker compose logs -f [service]`
- **Metrics**: Check Grafana dashboards
- **Support**: open a [GitHub Issue](https://github.com/fawadrafiq-1990/lpms/issues)

## 📝 License

This project is licensed under the [MIT License](./LICENSE).

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on setting up a
development environment and submitting changes.

## 📞 Support

- **Bugs & feature requests**: open a [GitHub Issue](https://github.com/fawadrafiq-1990/lpms/issues)
- **Questions**: start a [GitHub Discussion](https://github.com/fawadrafiq-1990/lpms/discussions)

---

**Version**: 1.0.0
