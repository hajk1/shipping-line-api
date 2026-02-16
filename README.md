# Freight Operations API

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

| Command                        | Description                     |
|--------------------------------|---------------------------------|
| `./mvnw clean install`         | Build + run tests               |
| `./mvnw spring-boot:run`       | Start the app                   |
| `./mvnw test`                  | Run tests only (H2, no Docker)  |
| `./mvnw fmt:format`            | Format code (Google style)      |
| `docker compose -f docker/docker-compose.yml up -d`   | Start PostgreSQL   |
| `docker compose -f docker/docker-compose.yml down -v`  | Stop + delete data |

## Contributing

Ready to pick up a task? See [CONTRIBUTING.md](CONTRIBUTING.md) for workflow, branch naming, and PR
guidelines.

- **Phase 1** — Core CRUD and foundations: [ISSUES.md](ISSUES.md)
- **Phase 2** — Pricing, invoicing, vessel planning, finance, and
  commissions: [ISSUES-PHASE2.md](ISSUES-PHASE2.md)

All issues use a domain-prefixed naming convention (e.g. `PRC-001`, `VPL-002`) so dependencies are
easy to follow. See the naming table at the top of each issues file.

## Tips for Contributors

- **Don't skip the DTO layer** — never expose JPA entities directly in REST responses.
- **Use `@Transactional(readOnly = true)`** on read-only service methods for better performance.
- **Fetch type is LAZY** on all `@ManyToOne` relations — be mindful of `LazyInitializationException` if you access relations outside a transaction.
- **Validation** is handled via Jakarta annotations (`@NotNull`, `@NotBlank`, etc.) — the `GlobalExceptionHandler` converts these into clean 400 responses automatically.
- Check existing repositories for query method naming conventions before writing custom `@Query`.