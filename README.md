# Freight Operations API

![CI](https://github.com/hajk1/shipping-line-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21+-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green?logo=springboot)
![License](https://img.shields.io/badge/License-MIT-yellow)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen)

A Spring Boot backend for a shipping line operations platform. Internal teams manage vessels,
voyages, freight orders, customers, agents, and invoices. Features include TEU-based capacity
control, PDF invoice generation with QR tracking codes, AI-powered price suggestions, and a
provider-agnostic LLM abstraction layer.

## Domain Model

```
Port  ←──  Voyage  ──→  Port
              │
           Vessel (TEU capacity / DWT)
              │
       FreightOrder  ──→  Container (TEU-based)
              │        or BulkCargo (tonnes-based) [planned]
              │
           Customer
           Operator
           Agent
```

**Core entities:**

| Entity        | Key field(s)                                   | Notes                                         |
|---------------|------------------------------------------------|-----------------------------------------------|
| Port          | `unlocode` (5 chars, UN/LOCODE)                | e.g. `AEJEA` for Jebel Ali                    |
| Vessel        | `imoNumber` (7 digits)                         | Carries `capacityTeu`; DWT planned            |
| Container     | `containerCode` (ISO 6346, 11 chars)           | Size: 20 / 40 ft · Type: DRY, REEFER, …       |
| Voyage        | `voyageNumber` (unique)                        | departure → arrival port, vessel, status      |
| FreightOrder  | FK to Voyage + Container + Customer + Operator | Priced from `VoyagePrice`, supports discounts |
| VoyagePrice   | (`voyageId`, `containerSize`)                  | Base price in USD per container size          |
| Customer      | `companyName`, `email`                         | Linked to freight orders                      |
| Agent         | `name`, `agentType`, `commissionPercent`       | Freight forwarder or port agent               |
| VesselOwner   | `name`, `sharePercent`                         | Multi-owner support                           |
| Invoice       | FK to FreightOrder                             | PDF with embedded QR code                     |
| TrackingEvent | FK to FreightOrder                             | Event log for shipment lifecycle              |

## Prerequisites

| Tool           | Version |
|----------------|---------|
| Java (JDK)     | 21+     |
| Maven          | 3.8+    |
| Docker         | 20+     |
| Docker Compose | 2+      |

## Quick Start

### 1. Start PostgreSQL

```bash
cd docker
docker compose up -d
```

This creates a PostgreSQL 16 instance at `localhost:5432` (database `freightops`,
credentials `freight/freight`).

### 2. Build & Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**. On first boot, `data.sql` seeds sample ports, a
vessel, and containers.

### 3. Configure Email (Optional - for invoice sending)

The application includes email functionality using SMTP. For local development, use **MailHog** to capture emails without sending them:

**Start MailHog:**

```bash
docker run --rm -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

- SMTP server: `localhost:1025` (configured in `application.properties`)
- Web UI: **http://localhost:8025** — view all captured emails in real-time

The `application.properties` is pre-configured:
```properties
spring.mail.host=localhost
spring.mail.port=1025
app.email.enabled=true
app.email.from-address=noreply@apgl-shipping.com
app.email.reply-to=support@apgl-shipping.com
```

To disable email sending (e.g., in tests):
```properties
app.email.enabled=false
```

### 4. Try the API

**Create a vessel:**

```bash
curl -X POST http://localhost:8080/api/v1/vessels \
  -H 'Content-Type: application/json' \
  -d '{"name": "MSC Gülsün", "imoNumber": "9811000", "capacityTeu": 23756}'
```

**Create a voyage:**

```bash
curl -X POST http://localhost:8080/api/v1/voyages \
  -H 'Content-Type: application/json' \
  -d '{
    "voyageNumber": "VOY-2026-001",
    "vesselId": 1,
    "departurePortId": 1,
    "arrivalPortId": 2,
    "departureTime": "2026-05-01T08:00:00",
    "arrivalTime": "2026-05-15T18:00:00"
  }'
```

**Create a freight order:**
```bash
curl -X POST http://localhost:8080/api/v1/freight-orders \
  -H 'Content-Type: application/json' \
  -d '{
    "voyageId": 1,
    "containerId": 1,
    "customerId": 1,
    "operatorId": 1,
    "notes": "Fragile cargo"
  }'
```

**Generate an invoice PDF:**
```bash
curl http://localhost:8080/api/v1/freight-orders/1/invoice --output invoice.pdf
```

**Send invoice to customer email:**
```bash
curl -X POST http://localhost:8080/api/v1/invoices/1/send
```

(Requires order status = DELIVERED; email is sent to customer's registered email address)

## Running Tests

Tests use an **H2 in-memory database** — no PostgreSQL needed.

```bash
./mvnw test
```

For the full build with coverage report:

```bash
./mvnw clean verify
# Open target/site/jacoco/index.html
```

## API Documentation (Swagger UI)

Once the application is running:
**http://localhost:8080/swagger-ui/index.html**

Explore all endpoints, see request/response schemas, and try the API from the browser.

## Project Structure

```
src/main/java/com/shipping/freightops/
├── FreightOpsApplication.java
├── ai/                          # LLM abstraction (Claude, OpenAI, NoOp implementations)
├── config/
│   ├── GlobalExceptionHandler.java
│   ├── PageableConfig.java
│   └── BookingProperties.java   # TEU cutoff threshold configuration
├── controller/
│   ├── FreightOrderController.java   # ★ reference implementation
│   ├── VoyageController.java
│   ├── VesselController.java
│   ├── ContainerController.java
│   ├── PortController.java
│   ├── CustomerController.java
│   ├── AgentController.java
│   ├── VesselOwnerController.java
│   ├── InvoiceController.java
│   └── TrackingController.java
├── dto/                         # Request / Response DTOs — never expose entities directly
├── entity/
│   ├── BaseEntity.java          # Shared id + audit timestamps
│   ├── Port.java
│   ├── Vessel.java
│   ├── Container.java
│   ├── Voyage.java
│   ├── FreightOrder.java
│   ├── VoyagePrice.java
│   ├── VoyageCost.java
│   ├── Customer.java
│   ├── Agent.java
│   ├── VesselOwner.java
│   ├── Invoice.java
│   └── TrackingEvent.java
├── enums/
│   ├── ContainerSize.java
│   ├── ContainerType.java
│   ├── OrderStatus.java
│   ├── VoyageStatus.java
│   └── AgentType.java
├── exception/
│   └── BadRequestException.java
├── repository/                  # Spring Data JPA repositories
└── service/                     # Business logic
```

## Code Style

This project uses [Google Java Format](https://github.com/google/google-java-format). The Maven
build auto-formats on compile via `fmt-maven-plugin`.

```bash
./mvnw fmt:format    # reformat all sources
./mvnw fmt:check     # check without changing (used in CI)
```

**IDE setup:**

- **IntelliJ:** Install the "google-java-format" plugin → Settings → google-java-format → Enable;
  also enable annotation processing for Lombok
- **VS Code:** Install "Google Java Format" and "Lombok Annotations Support" extensions

## Useful Commands

| Command                                               | Description                    |
|-------------------------------------------------------|--------------------------------|
| `./mvnw clean install`                                | Build + run tests              |
| `./mvnw clean verify`                                 | Build + test + coverage report |
| `./mvnw spring-boot:run`                              | Start the app                  |
| `./mvnw test`                                         | Run tests only (H2, no Docker) |
| `./mvnw fmt:format`                                   | Format code (Google style)     |
| `./mvnw fmt:check`                                    | Check format without changing  |
| `docker compose -f docker/docker-compose.yml up -d`   | Start PostgreSQL               |
| `docker compose -f docker/docker-compose.yml down -v` | Stop + delete data             |

## CI / GitHub Actions

Every push to `master`/`develop` and every PR triggers:

1. **Build & Test** — `./mvnw clean verify` with JDK 21
2. **Format Check** — `./mvnw fmt:check` fails if code is not Google-formatted
3. **Test Coverage** — JaCoCo report posted as a PR comment; minimums: 40% overall, 60% on
   changed files
4. **Test Results** — Surefire results published as a GitHub check

Coverage reports are uploaded as build artifacts (14-day retention).

## Contributing

Ready to pick up a task? See [CONTRIBUTING.md](CONTRIBUTING.md) for workflow, branch naming, and
PR guidelines.

- **Phase 1** — Core CRUD and foundations: [docs/ISSUES.md](docs/ISSUES.md)
- **Phase 2** — Pricing, invoicing, vessel planning, finance, tracking, AI pricing:
  [docs/ISSUES-PHASE2.md](docs/ISSUES-PHASE2.md)
- **Phase 3** — Infrastructure hardening, data model cleanup, bulk cargo:
  [docs/ISSUES-PHASE3.md](docs/ISSUES-PHASE3.md)

All issues use a domain-prefixed naming convention (`INF-001`, `CRG-001`, etc.) so dependencies
are easy to follow. See the naming tables at the top of each issues file.

For the big picture see [docs/ROADMAP.md](docs/ROADMAP.md). For a non-technical overview of all
flows and the data model see [docs/stakeholder-overview.md](docs/stakeholder-overview.md).

## Key Conventions for Contributors

- **DTO layer is mandatory** — never expose JPA entities directly in REST responses.
- **`@Transactional(readOnly = true)`** on read-only service methods.
- **All `@ManyToOne` are LAZY** — always access associations inside a `@Transactional` boundary to
  avoid `LazyInitializationException`.
- **Validation** via Jakarta annotations; `GlobalExceptionHandler` converts violations to clean 400
  responses automatically.
- **Not-found cases** must throw `ResponseStatusException(NOT_FOUND)`, not
  `IllegalArgumentException` — the former becomes 404, the latter becomes 500.
- **Format before committing** — `./mvnw fmt:format`; CI will reject unformatted code.
