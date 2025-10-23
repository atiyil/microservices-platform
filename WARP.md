# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a **Spring Boot 3 microservices platform** using Java 17, Gradle with Kotlin DSL, and designed for Kubernetes deployment. The platform follows a **monorepo structure** with separate services and shared libraries.

**Current Status:** Only `user-service` is fully implemented. Other services (api-gateway, product-service, order-service, notification-service) are planned but not yet created.

## Architecture

### Monorepo Structure
- `services/` - Individual microservices (each is a separate Gradle subproject)
- `shared-libraries/` - Common code (directories exist but not yet implemented in Gradle)
- `kubernetes/` - K8s deployment manifests
- `docker-compose/` - Local infrastructure (PostgreSQL, Redis, Prometheus, Grafana)

### Service Architecture Pattern
Each service follows Spring Boot's standard layered architecture:
- `controller/` - REST endpoints
- `service/` - Business logic
- `repository/` - Data access (Spring Data JPA)
- `model/` - JPA entities
- `dto/` - Request/Response DTOs
- `exception/` - Custom exceptions and global exception handlers
- `config/` - Spring configuration classes

### Key Technologies
- **Java 17** with Lombok for boilerplate reduction
- **Gradle 8.5** with Kotlin DSL and version catalog (`gradle/libs.versions.toml`)
- **Spring Boot 3.2** with Spring Data JPA, Redis, Actuator
- **MapStruct** for DTO mapping
- **Resilience4j** for circuit breaker patterns
- **PostgreSQL** (prod) and H2 (dev) databases
- **Redis** for caching
- **Prometheus/Grafana** for monitoring

## Development Commands

### Build & Run
```bash
# Build entire project
./gradlew build

# Build specific service
./gradlew :services:user-service:build

# Run service locally (uses H2 in-memory DB, dev profile)
./gradlew :services:user-service:bootRun

# Run with prod profile (requires PostgreSQL & Redis from docker-compose)
./gradlew :services:user-service:bootRun --args='--spring.profiles.active=prod'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for specific service
./gradlew :services:user-service:test

# Generate test coverage report
./gradlew jacocoTestReport
```

### Docker & Kubernetes
```bash
# Start local infrastructure (PostgreSQL, Redis, Prometheus, Grafana)
cd docker-compose && docker-compose up -d

# Build Docker image (must run from project root)
docker build -t user-service:latest -f services/user-service/Dockerfile .

# Deploy to Kubernetes
kubectl apply -f kubernetes/user-service-deployment.yaml
```

## Configuration Profiles

The project uses Spring profiles for environment-specific configuration:

- **dev** (default) - H2 in-memory database, no Redis, no Kubernetes discovery, SQL logging enabled
- **prod** - PostgreSQL, Redis, connection pooling, no SQL logging
- **kubernetes** - Full production config with K8s service discovery and config/secret management

Profile configuration is in `services/user-service/src/main/resources/application.yml` with profile-specific sections delimited by `---`.

## Service Details

### user-service
- **Port:** 8080
- **Context Path:** `/api/v1`
- **Base Endpoint:** `/api/v1/users`
- **Database:** Users with CRUD, search, pagination, soft delete
- **Features:** Redis caching, circuit breaker, validation, health checks

### Planned Services (not yet implemented)
- `api-gateway` - Entry point for all requests
- `product-service` - Product catalog management
- `order-service` - Order processing
- `notification-service` - Email/SMS notifications

## Adding New Services

When creating a new service:

1. Create directory: `services/<service-name>/`
2. Add to `settings.gradle.kts`: `include("services:<service-name>")`
3. Create `services/<service-name>/build.gradle.kts` using version catalog references
4. Follow the package structure: `com.microservices.<servicename>.{controller,service,repository,model,dto,exception,config}`
5. Use MapStruct for entity-DTO conversions
6. Add Resilience4j circuit breakers for inter-service calls
7. Include actuator health checks
8. Create Kubernetes manifests following the pattern in `kubernetes/user-service-deployment.yaml`

## Testing Guidelines

- **Integration tests** use `@SpringBootTest`, `@AutoConfigureMockMvc`, and `@ActiveProfiles("dev")`
- Tests are `@Transactional` to auto-rollback database changes
- Use `MockMvc` for controller testing
- Test classes are in `src/test/java` mirroring the main package structure
- Current test coverage includes controller integration tests and service unit tests

## Monitoring & Observability

All services expose actuator endpoints at `/api/v1/actuator/`:
- `/health` - Kubernetes liveness/readiness probes
- `/prometheus` - Metrics scraping
- `/metrics` - General metrics
- `/info` - Application info

Prometheus scrapes metrics from services, Grafana visualizes them (accessible at `localhost:3000` when running docker-compose).

## Important Notes

- All services must be built from the **project root**, not from individual service directories
- Gradle wrapper (`./gradlew`) should always be used instead of local Gradle installation
- Docker builds must run from project root and copy the entire workspace
- Database credentials in Kubernetes use Secrets; local dev uses environment variables with defaults
- Services use soft delete (status=DELETED) rather than hard deletes
- The shared libraries directories exist but are not yet added to Gradle settings
