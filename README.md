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

The API will be available at `http://localhost:8080`. You can access the Swagger UI documentation at `http://localhost:8080/swagger-ui.html`.

## Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository and create your branch from `main`.
2. Ensure your code follows the existing style and passes all tests (`./mvnw test`).
3. Submit a Pull Request with a clear description of your changes.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.