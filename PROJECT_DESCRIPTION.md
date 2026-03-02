# Shipping Line API - Project Description

## 🚢 Project Overview

**Freight Operations API** is a Spring Boot proof-of-concept (POC) backend system designed for managing shipping line freight operations. The application enables internal operations teams to schedule vessel voyages between ports and book containers (freight orders) onto those voyages.

**Project Name:** freight-ops  
**Version:** 0.1.0-SNAPSHOT  
**Java Version:** 21  
**Spring Boot Version:** 3.4.5  
**Build Tool:** Maven

---

## 🏗️ Architecture & Domain Model

The system models shipping operations around five core entities:

```
Port  ←──  Voyage  ──→  Port
              │
              │   Vessel
              │
       FreightOrder
              │
         Container (20ft / 40ft, DRY / REEFER / …)
```

### Core Entities

#### **Port**
- Represents a seaport location
- Identified by UN/LOCODE (e.g., `AEJEA` for Jebel Ali, Dubai)
- Serves as departure and arrival points for voyages

#### **Vessel**
- Represents a cargo ship
- Identified by a 7-digit IMO (International Maritime Organization) number
- Assigned to voyages to carry freight

#### **Container**
- Represents cargo containers used in shipping
- Identified by ISO 6346 code
- Attributes:
  - **Size:** 20-foot or 40-foot containers
  - **Type:** DRY, REEFER, OPEN_TOP, FLAT_RACK, TANK

#### **Voyage**
- Represents a scheduled vessel trip
- Links a departure port to an arrival port
- Includes:
  - Vessel assignment
  - Departure and arrival times
  - Status tracking (PLANNED, IN_TRANSIT, COMPLETED, CANCELLED)
- Can accommodate multiple freight orders via containers

#### **FreightOrder**
- Represents the booking of a container onto a voyage
- Links a container to a specific voyage
- Tracks:
  - Order creator (`orderedBy`)
  - Additional notes
  - Order status
- **Constraint:** Cannot book freight on cancelled voyages

---

## 📁 Project Structure

```
shipping-line-api/
├── docker/
│   └── docker-compose.yml          # PostgreSQL containerization
├── src/
│   ├── main/
│   │   ├── java/com/shipping/freightops/
│   │   │   ├── FreightOpsApplication.java       # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   └── GlobalExceptionHandler.java  # Centralized exception handling
│   │   │   ├── controller/
│   │   │   │   └── FreightOrderController.java  # REST API endpoints
│   │   │   ├── dto/
│   │   │   │   ├── CreateFreightOrderRequest.java
│   │   │   │   └── FreightOrderResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── BaseEntity.java              # Base JPA entity (timestamps)
│   │   │   │   ├── Container.java
│   │   │   │   ├── FreightOrder.java
│   │   │   │   ├── Port.java
│   │   │   │   ├── Vessel.java
│   │   │   │   └── Voyage.java
│   │   │   ├── enums/
│   │   │   │   ├── ContainerSize.java
│   │   │   │   ├── ContainerType.java
│   │   │   │   ├── OrderStatus.java
│   │   │   │   └── VoyageStatus.java
│   │   │   ├── repository/
│   │   │   │   ├── ContainerRepository.java
│   │   │   │   ├── FreightOrderRepository.java
│   │   │   │   ├── PortRepository.java
│   │   │   │   ├── VesselRepository.java
│   │   │   │   └── VoyageRepository.java
│   │   │   └── service/
│   │   │       └── FreightOrderService.java     # Business logic layer
│   │   └── resources/
│   │       ├── application.properties           # Configuration (DB, port, etc.)
│   │       └── data.sql                         # Initial seed data
│   └── test/
│       ├── java/com/shipping/freightops/
│       │   └── controller/
│       │       └── FreightOrderControllerTest.java
│       └── resources/
│           └── application.properties           # H2 in-memory config for tests
├── pom.xml                                      # Maven configuration
├── mvnw / mvnw.cmd                             # Maven wrapper scripts
└── README.md                                    # Quick start guide
```

---

## 🔧 Technology Stack

### Backend Framework
- **Spring Boot 3.4.5** – Rapid application development
- **Spring Data JPA** – Object-relational mapping (ORM)
- **Spring MVC** – REST API layer
- **Spring Validation** – Input validation (Jakarta Bean Validation)

### Database
- **PostgreSQL 16** – Production database (runtime)
- **H2** – In-memory testing database

### Build & Deployment
- **Maven 3.8+** – Build automation
- **Docker & Docker Compose** – Containerization

### Development
- **Java 21** – Latest LTS release
- **Hibernate** – JPA implementation
- **Jackson** – JSON serialization/deserialization

---

## 🚀 API Endpoints

### Freight Order Management
The application currently provides full REST CRUD operations for freight orders:

#### **Create Freight Order** (POST)
```
POST /api/v1/freight-orders
Content-Type: application/json

{
  "voyageId": 1,
  "containerId": 1,
  "orderedBy": "ops-team",
  "notes": "Fragile cargo"
}
```
- **Response:** 201 Created with `Location` header

#### **Get Freight Order by ID** (GET)
```
GET /api/v1/freight-orders/{id}
```
- **Response:** 200 OK with FreightOrderResponse

#### **List All Freight Orders** (GET)
```
GET /api/v1/freight-orders
GET /api/v1/freight-orders?voyageId=1  # Filter by voyage
```
- **Response:** 200 OK with list of orders

---

## 🗄️ Database Configuration

### Connection Details (Docker)
- **Host:** localhost
- **Port:** 5432
- **Database:** freightops
- **Username:** freight
- **Password:** freight
- **Driver:** PostgreSQL JDBC

### Initialization
- **DDL Mode:** `update` (Hibernate auto-creates/modifies schema)
- **Seed Data:** `data.sql` loaded on startup
- **Deferred Initialization:** Enabled to allow Hibernate schema creation before data loading

---

## 📊 Application Configuration

Located in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Postxxxxxxxxxxxxction
spring.datasource.url=jdbc:postgresql://localhost:5432/freightops
spring.datasource.username=freight
spring.datasource.password=freight

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.show-sql=true

# SQL Seed Data
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# JSON Formatting
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 🧪 Testing

### Test Setup
- **Framework:** JUnit 5 (via Spring Boot)
- **Database:** H2 in-memory (no PostgreSQL required)
- **Location:** `src/test/java/com/shipping/freightops/`

### Running Tests
```bash
./mvnw test
```

### Test Coverage
- **FreightOrderControllerTest** – API endpoint testing
- Tests use isolated H2 database with separate configuration

---

## 🚀 Quick Start Guide

### Prerequisites
| Tool           | Version  | Purpose                          |
|----------------|----------|----------------------------------|
| Java (JDK)     | 21+      | Language runtime                 |
| Maven          | 3.8+     | Build and dependency management |
| Docker         | 20+      | Container runtime               |
| Docker Compose | 2+       | Multi-container orchestration    |

### Step 1: Start PostgreSQL
```bash
cd docker
docker compose up -d
```
Starts PostgreSQL 16 at `localhost:5432`

### Step 2: Build & Run Application
```bash
./mvnw clean install
./mvnw spring-boot:run
```
- On first startup, Hibernate creates tables
- `data.sql` seeds sample data

### Step 3: Verify Server
```bash
curl http://localhost:8080/api/v1/freight-orders
```

---

## 📝 Notes for Development

### Current Implementation Status
✅ **Complete:**
- FreightOrder entity, repository, service, and controller
- Exception handling
- Data persistence layer
- REST API for freight orders

⏳ **TODO (POC Phase):**
- Voyage management controller (reference: FreightOrderController)
- Container management controller
- Port management controller  
- Vessel management controller
- Authentication/authorization
- API documentation (Swagger/OpenAPI)
- Additional business logic (capacity checking, voyage status transitions, etc.)

### Design Patterns Used
- **Repository Pattern** – Data access abstraction
- **Service Layer Pattern** – Business logic separation
- **DTO Pattern** – Request/response transformation
- **Exception Handling** – Global exception handling via `GlobalExceptionHandler`
- **Transactional Management** – Spring `@Transactional` annotations

### Key Features
- ✅ RESTful API design
- ✅ Input validation (Jakarta Bean Validation)
- ✅ Transaction management
- ✅ Domain-driven design
- ✅ Clean separation of concerns
- ✅ Seed data initialization
- ✅ Docker containerization

---

## 🔗 Key Files Reference

| File | Purpose |
|------|---------|
| [FreightOpsApplication.java](src/main/java/com/shipping/freightops/FreightOpsApplication.java) | Spring Boot entry point |
| [FreightOrderController.java](src/main/java/com/shipping/freightops/controller/FreightOrderController.java) | REST API example (use as reference) |
| [FreightOrderService.java](src/main/java/com/shipping/freightops/service/FreightOrderService.java) | Business logic example |
| [FreightOrder.java](src/main/java/com/shipping/freightops/entity/FreightOrder.java) | Core entity definition |
| [pom.xml](pom.xml) | Maven dependencies & build configuration |
| [application.properties](src/main/resources/application.properties) | Runtime configuration |
| [docker-compose.yml](docker/docker-compose.yml) | PostgreSQL setup |

---

## 📚 Related Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- PostgreSQL: https://www.postgresql.org/
- Docker: https://docs.docker.com/

---

**Last Updated:** February 16, 2026  
**Project Status:** Proof of Concept (POC)  
**Owner:** hajk1 (GitHub)
