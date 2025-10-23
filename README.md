# Microservices Platform

A RESTful API microservices platform built with **Java 17**, **Spring Boot 3**, **Gradle**, and **Kubernetes**.

## 🏗️ Architecture

This is a **monorepo** containing multiple microservices:

```
microservices-platform/
├── services/
│   ├── user-service/          ✅ Implemented
│   ├── api-gateway/
│   ├── product-service/
│   ├── order-service/
│   └── notification-service/
├── shared-libraries/
│   ├── common-dto/
│   ├── security-config/
│   └── monitoring-config/
├── kubernetes/                 ✅ K8s manifests
└── docker-compose/             
```

## 🚀 Tech Stack

- **Java 17** - Latest LTS version
- **Spring Boot 3.2** - Framework
- **Gradle 8.5** with Kotlin DSL - Build tool
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Kubernetes** - Container orchestration
- **Docker** - Containerization
- **Resilience4j** - Circuit breaker
- **Prometheus** - Metrics
- **Spring Cloud Kubernetes** - Service discovery

## 📦 Services

### User Service (✅ Completed)

**Port:** 8080  
**Base Path:** `/api/v1/users`

**Features:**
- ✅ Full CRUD operations
- ✅ User search and filtering
- ✅ Pagination and sorting
- ✅ Input validation
- ✅ Global exception handling
- ✅ Redis caching
- ✅ Circuit breaker pattern
- ✅ Health checks and metrics
- ✅ Soft delete support

**API Endpoints:**
```
POST   /api/v1/users                 - Create user
GET    /api/v1/users/{id}            - Get user by ID
GET    /api/v1/users/username/{name} - Get user by username
GET    /api/v1/users                 - Get all users (paginated)
GET    /api/v1/users/search          - Search users
PUT    /api/v1/users/{id}            - Update user
PATCH  /api/v1/users/{id}/status     - Update user status
DELETE /api/v1/users/{id}            - Soft delete user
```

## 🛠️ Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Kubernetes (minikube/kind for local)
- Gradle 8+ (or use wrapper)

### Local Development

1. **Clone the repository:**
```bash
cd microservices-platform
```

2. **Build the project:**
```bash
./gradlew build
```

3. **Run User Service locally (H2 database):**
```bash
./gradlew :services:user-service:bootRun
```

The service will start on `http://localhost:8080` with H2 console at `/h2-console`.

4. **Run with Docker Compose (PostgreSQL + Redis):**
```bash
cd docker-compose
docker-compose up -d
./gradlew :services:user-service:bootRun --args='--spring.profiles.active=prod'
```

### Build Docker Image

```bash
docker build -t user-service:latest -f services/user-service/Dockerfile .
```

### Deploy to Kubernetes

```bash
kubectl apply -f kubernetes/user-service-deployment.yaml
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific service tests
./gradlew :services:user-service:test

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📊 Monitoring

- **Health Check:** `/api/v1/actuator/health`
- **Metrics:** `/api/v1/actuator/metrics`
- **Prometheus:** `/api/v1/actuator/prometheus`

## 🔧 Configuration Profiles

- **dev** - H2 in-memory database, console enabled
- **prod** - PostgreSQL, Redis
- **kubernetes** - For K8s deployment with service discovery

## 📝 Example Requests

### Create User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890"
  }'
```

### Get All Users (Paginated)
```bash
curl "http://localhost:8080/api/v1/users?page=0&size=10&sortBy=createdAt&sortDirection=DESC"
```

### Search Users
```bash
curl "http://localhost:8080/api/v1/users/search?query=john&status=ACTIVE"
```

## 🏗️ Next Steps

- [ ] Implement API Gateway service
- [ ] Add Spring Security + JWT authentication
- [ ] Implement Product Service
- [ ] Implement Order Service
- [ ] Add Kafka for async communication
- [ ] Set up CI/CD pipeline
- [ ] Add integration tests
- [ ] Implement distributed tracing (Zipkin/Jaeger)
- [ ] Add API documentation (Swagger/OpenAPI)

## 📚 Project Structure

```
services/user-service/
├── src/
│   ├── main/
│   │   ├── java/com/microservices/userservice/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Data access
│   │   │   ├── model/           # JPA entities
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── exception/       # Custom exceptions & handlers
│   │   │   └── config/          # Configuration classes
│   │   └── resources/
│   │       └── application.yml  # Configuration
│   └── test/                    # Unit & integration tests
├── Dockerfile
└── build.gradle.kts
```

## 🤝 Contributing

This is a learning/demonstration project. Feel free to fork and experiment!

## 📄 License

MIT
