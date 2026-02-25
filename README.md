# Freight Operations API

![CI](https://github.com/hajk1/shipping-line-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21+-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green?logo=springboot)
![License](https://img.shields.io/badge/License-MIT-yellow)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen)

A Spring Boot POC for a shipping line backend. Internal operations teams can create voyages between ports and book containers (freight orders) onto those voyages.

## Domain Model

```
Port  ←──  Voyage  ──→  Port
              │
              │   Vessel
              │
       FreightOrder
              │
         Container (20ft / 40ft, DRY / REEFER / …)
```

**Key entities:**

- **Port** – identified by UN/LOCODE (e.g. `AEJEA` for Jebel Ali)
- **Vessel** – identified by 7-digit IMO number
- **Container** – ISO 6346 code, size (20/40 foot), type (DRY, REEFER, OPEN_TOP, FLAT_RACK, TANK)
- **Voyage** – a scheduled vessel trip from departure port → arrival port
- **FreightOrder** – books a container onto a voyage, placed by an internal team member

## Prerequisites

| Tool          | Version  |
|---------------|----------|
| Java (JDK)    | 21+      |
| Maven         | 3.8+     |
| Docker        | 20+      |
| Docker Compose| 2+       |

## Quick Start

### 1. Start PostgreSQL

```bash
cd docker
docker compose up -d
```

This creates a PostgreSQL 16 instance at `localhost:5432` with database `freightops` and credentials `freight/freight`.

### 2. Build & Run

```bash
# From the project root
./mvnw clean install

# Run the app
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**. On first boot, Hibernate creates the tables and `data.sql` seeds sample ports, a vessel, and a few containers.

### 3. Try the API

**Create a voyage first** (there's no controller for this yet — that's your job!), or insert one directly:

```sql
-- Connect to postgres: docker exec -it freightops-db psql -U freight -d freightops
INSERT INTO voyages (voyage_number, vessel_id, departure_port_id, arrival_port_id,
                     departure_time, arrival_time, status, created_at, updated_at)
VALUES ('VOY-2025-001', 1, 1, 2, '2025-04-01 08:00', '2025-04-15 18:00', 'PLANNED', NOW(), NOW());
```

**Create a freight order:**

```bash
curl -X POST http://localhost:8080/api/v1/freight-orders \
  -H 'Content-Type: application/json' \
  -d '{
    "voyageId": 1,
    "containerId": 1,
    "orderedBy": "ops-team",
    "notes": "Fragile cargo"
  }'
```

**List all freight orders:**

```bash
curl http://localhost:8080/api/v1/freight-orders
```

**Get a single order:**

```bash
curl http://localhost:8080/api/v1/freight-orders/1
```

## Running Tests

Tests use an **H2 in-memory database** — no PostgreSQL needed.

```bash
./mvnw test
```

Look at `FreightOrderControllerTest.java` for a working example of how to write integration tests with MockMvc and JUnit 5 (Jupiter).

## API Documentation (Swagger UI)

The API is documented with **OpenAPI/Swagger**. Once the application is running, you can access the interactive Swagger UI at:
**http://localhost:8080/swagger-ui/index.html**


From there, you can explore endpoints, see request/response schemas, and try out the API directly from the browser.

💡 Note: The older `/swagger-ui.html` path is automatically redirected to `/swagger-ui/index.html`.



## Project Structure

```
src/main/java/com/shipping/freightops/
├── FreightOpsApplication.java       # Entry point
├── config/
│   └── GlobalExceptionHandler.java  # Centralized error handling
├── controller/
│   └── FreightOrderController.java  # ★ Sample controller — follow this pattern
├── dto/
│   ├── CreateFreightOrderRequest.java
│   └── FreightOrderResponse.java
├── entity/
│   ├── BaseEntity.java              # Shared id + audit fields
│   ├── Container.java
│   ├── FreightOrder.java
│   ├── Port.java
│   ├── Vessel.java
│   └── Voyage.java
├── enums/
│   ├── ContainerSize.java
│   ├── ContainerType.java
│   ├── OrderStatus.java
│   └── VoyageStatus.java
├── repository/
│   ├── ContainerRepository.java
│   ├── FreightOrderRepository.java
│   ├── PortRepository.java
│   ├── VesselRepository.java
│   └── VoyageRepository.java
└── service/
    └── FreightOrderService.java
```

## What You Need to Build

This POC has **one working controller** (`FreightOrderController`). Your tasks:

1. **VoyageController** – CRUD for voyages (create a voyage between two ports on a vessel)
2. **ContainerController** – CRUD for containers
3. **PortController** – CRUD for ports
4. **VesselController** – CRUD for vessels

For each controller, follow the same pattern:

1. Create a **Request DTO** (e.g. `CreateVoyageRequest`) with validation annotations
2. Create a **Response DTO** (e.g. `VoyageResponse`) with a `fromEntity()` factory method
3. Create a **Service** class with business logic
4. Create a **Controller** with REST endpoints
5. Write a **test** class following `FreightOrderControllerTest` as a template

## Code Style

This project uses [Google Java Format](https://github.com/google/google-java-format). The Maven build auto-formats on compile via the `fmt-maven-plugin`.

To manually format:

```bash
./mvnw fmt:format
```

To check formatting without changing files:

```bash
./mvnw fmt:check
```

**IDE setup:**
- **IntelliJ**: Install the "google-java-format" plugin → Settings → google-java-format → Enable
- **VS Code**: Use the "Google Java Format" extension

## Useful Commands

| Command                                               | Description                    |
|-------------------------------------------------------|--------------------------------|
| `mvn clean install`                                   | Build + run tests              |
| `mvn clean verify`                                    | Build + test + coverage report |
| `mvn spring-boot:run`                                 | Start the app                  |
| `mvn test`                                            | Run tests only (H2, no Docker) |
| `mvn fmt:format`                                      | Format code (Google style)     |
| `mvn fmt:check`                                       | Check format without changing  |
| `docker compose -f docker/docker-compose.yml up -d`   | Start PostgreSQL               |
| `docker compose -f docker/docker-compose.yml down -v` | Stop + delete data             |

After `mvn clean verify`, open `target/site/jacoco/index.html` to browse the coverage report
locally.

## CI / GitHub Actions

Every push to `master`/`develop` and every PR triggers the CI pipeline:

1. **Build & Test** — `mvn clean verify` with JDK 21
2. **Format Check** — `mvn fmt:check` fails the build if code isn't Google-formatted
3. **Test Coverage** — JaCoCo generates a report, posted as a PR comment with coverage diff
4. **Test Results** — Surefire results published as a GitHub check

Coverage reports are uploaded as build artifacts and retained for 14 days. On PRs, the bot posts a
comment with overall coverage and per-file diff — minimum thresholds are 40% overall and 60% on
changed files.

To run the same checks locally before pushing:

```bash
mvn clean verify     # build + test + coverage report
mvn fmt:check        # format check (no changes)
```

Coverage HTML report is generated at `target/site/jacoco/index.html`.

## Contributing

Ready to pick up a task? See [CONTRIBUTING.md](CONTRIBUTING.md) for workflow, branch naming, and PR
guidelines.

- **Phase 1** — Core CRUD and foundations: [ISSUES.md](ISSUES.md)
- **Phase 2** — Pricing, invoicing, notifications, vessel planning, finance, commissions, barcode
  tracking, and AI pricing: [ISSUES-PHASE2.md](ISSUES-PHASE2.md)

All issues use a domain-prefixed naming convention (e.g. `PRC-001`, `VPL-002`) so dependencies are
easy to follow. See the naming table at the top of each issues file.

For the big picture on where this project is headed, see the [ROADMAP.md](ROADMAP.md).

## Tips for Contributors

- **Don't skip the DTO layer** — never expose JPA entities directly in REST responses.
- **Use `@Transactional(readOnly = true)`** on read-only service methods for better performance.
- **Fetch type is LAZY** on all `@ManyToOne` relations — be mindful of `LazyInitializationException` if you access relations outside a transaction.
- **Validation** is handled via Jakarta annotations (`@NotNull`, `@NotBlank`, etc.) — the `GlobalExceptionHandler` converts these into clean 400 responses automatically.
- Check existing repositories for query method naming conventions before writing custom `@Query`.