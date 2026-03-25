# Issues Backlog — Phase 3

Infrastructure hardening, data model cleanup, and bulk cargo support. These issues address
technical debt, close gaps in the existing API surface, and introduce a new cargo mode that extends
the platform beyond containers.

## Issue Naming Convention

All issues follow the pattern: `DOMAIN-NNN — Title`

| Prefix | Domain         |
|--------|----------------|
| `INF`  | Infrastructure |
| `ENH`  | Enhancements   |
| `CRG`  | Cargo          |

When referencing dependencies, use the full code (e.g. "Depends on `INF-001`").

Issues are labeled by difficulty: 🟢 Easy | 🟠 Challenging | 🔴 Complex

---

## INF-001 — Database Migrations with Flyway 🟠

**Labels:** `backend`, `infrastructure`

The schema is currently managed by `hibernate.ddl-auto=update`. On each startup Hibernate silently
mutates the database with no history, no audit trail, and no rollback path. An accidental column
rename or drop is unrecoverable. Flyway replaces this with versioned migration files that are
applied once, tracked in a checksummed history table, and never modified after the fact.

**What to create:**

- `flyway-core` dependency in `pom.xml`
- `src/main/resources/db/migration/V1__baseline.sql` — ANSI SQL baseline capturing all current
  tables: `ports`, `vessels`, `vessel_owners`, `containers`, `voyages`, `voyage_prices`,
  `voyage_costs`, `customers`, `agents`, `freight_orders`, `tracking_events`, `invoices`

**Configuration changes:**

- Set `spring.jpa.hibernate.ddl-auto=validate` in `application.properties` — Hibernate validates
  the schema against the entities; Flyway owns the DDL
- H2 test datasource must remain compatible (use ANSI SQL in the baseline; avoid
  PostgreSQL-specific syntax in `V1__`)

**Naming convention (document in a comment at the top of `V1__baseline.sql`):**

```
-- Migrations follow: V{version}__{description}.sql
-- Never edit an existing migration. Always add a new numbered file.
```

**Hints:**

- Run `./mvnw spring-boot:run` against the Docker PostgreSQL before writing the baseline — use
  `\d tablename` in psql to see the exact column types Hibernate generated, then transcribe them
- Add `spring.flyway.enabled=false` to `src/test/resources/application.properties` only if H2
  compatibility is not achievable; prefer keeping it enabled with ANSI SQL

**Acceptance criteria:**

- [ ] `flyway-core` added to `pom.xml`
- [ ] `spring.jpa.hibernate.ddl-auto=validate` in `application.properties`
- [ ] `V1__baseline.sql` covers all tables present in the current schema
- [ ] Migrations run on both PostgreSQL (prod) and H2 (tests) without error
- [ ] All existing tests pass unchanged after the change
- [ ] Naming convention documented in the baseline file header

---

## ENH-006 — Expose `id` on VesselResponse and VoyageResponse 🟢

**Labels:** `backend`, `enhancement`, `good-first-issue`

`VesselResponse` and `VoyageResponse` omit the entity `id`. A caller who creates a vessel or
voyage receives a response they cannot use in follow-up requests without making an extra GET call.
This breaks the standard REST convention where a POST response is self-sufficient.

**Current state:**

```java
// VesselResponse — id never mapped
public static VesselResponse fromEntity(Vessel vessel) {
    dto.name = vessel.getName();
    dto.imoNumber = vessel.getImoNumber();
    dto.capacityTeu = vessel.getCapacityTeu();
}

// VoyageResponse — id never mapped
public VoyageResponse(Voyage voyage) {
    voyageNumber = voyage.getVoyageNumber();
    vesselName = voyage.getVessel().getName();
}
```

**What to change:**

- Add `Long id` field to `VesselResponse`; set it in `fromEntity()`
- Add `Long id` field to `VoyageResponse`; set it in the constructor and in the `VoyageResponses()`
  list factory method

**Test changes:**

- `VesselControllerTest` — assert `$.id` is present and non-null on both POST and GET responses
- `VoyageControllerTest` — assert `$.id` is present and non-null on both POST and GET responses

**Acceptance criteria:**

- [ ] `VesselResponse` includes `id`; `fromEntity()` maps it
- [ ] `VoyageResponse` includes `id`; constructor and list factory both map it
- [ ] `VesselControllerTest` asserts `id` non-null on POST and GET
- [ ] `VoyageControllerTest` asserts `id` non-null on POST and GET
- [ ] All existing tests still pass
- [ ] Code is formatted

---

## ENH-007 — Test Coverage for Error Scenarios 🟠

**Labels:** `backend`, `enhancement`, `testing`

The test suite covers only the happy path. Manual API testing reveals unhandled edge cases: missing
entities return 500 instead of 404, business rule violations surface as unstructured errors, and
lazy associations accessed outside a transaction throw 500s. This issue adds negative-path coverage
and fixes the underlying service-layer issues that these tests expose.

**Use `FreightOrderControllerTest` as the reference pattern for test setup.**

**404 — Entity not found** (one test per controller):

- `VesselControllerTest` — `GET /vessels/9999` → 404
- `VoyageControllerTest` — `GET /voyages/9999` → 404
- `FreightOrderControllerTest` — `GET /freight-orders/9999` → 404
- `PortControllerTest` — `GET /ports/9999` → 404
- `CustomerControllerTest` — `GET /customers/9999` → 404
- `AgentControllerTest` — `GET /agents/9999` → 404
- `InvoiceControllerTest` — invoice for non-existent order ID → 404

**400 — Validation failures:**

- `VoyageControllerTest` — create voyage with missing required fields → 400
- `FreightOrderControllerTest` — create order with missing `voyageId`, `containerId`, or
  `customerId` → 400
- `FreightOrderControllerTest` — `discountPercent` > 100 → 400

**Business rule violations:**

- `FreightOrderControllerTest` — book the same container on two active voyages → 400
  (double-booking)
- `InvoiceControllerTest` — generate invoice for an order not in `DELIVERED` status → appropriate
  4xx (not 500)

**Underlying fixes required by these tests:**

- Any service method that throws `IllegalArgumentException` for a not-found case must throw
  `ResponseStatusException(NOT_FOUND)` instead — the `GlobalExceptionHandler` then returns 404
- Any service method that accesses a lazy association must be annotated `@Transactional` or
  `@Transactional(readOnly = true)` as appropriate

**Acceptance criteria:**

- [ ] All 404 tests listed above implemented and passing
- [ ] All 400 validation tests implemented and passing
- [ ] Business rule violation tests implemented and passing
- [ ] All services that return not-found throw `ResponseStatusException(NOT_FOUND)`, not 500
- [ ] All service methods accessing lazy associations are `@Transactional`
- [ ] No existing test regresses
- [ ] Code is formatted

---

## ENH-008 — Replace `orderedBy` Free-Text with Structured User Reference 🟠

**Labels:** `backend`, `enhancement`, `data-model`
**Depends on:** `INF-001`

`FreightOrder.orderedBy` is a free-text `String` with only a `@NotBlank` constraint. Any arbitrary
value is accepted (`"me"`, `"ops-team"`, `""`), there is no referential integrity, and orders
cannot be queried by a specific operator. This issue replaces the string field with an `Operator`
entity that represents an internal system user.

**New entity: `Operator`**

- `id` — BIGINT PK
- `username` — VARCHAR(50), UNIQUE, NOT NULL
- `fullName` — VARCHAR(100)
- `email` — VARCHAR(150), UNIQUE, NOT NULL
- `active` — BOOLEAN, DEFAULT TRUE
- `createdAt` / `updatedAt` — timestamps (via `BaseEntity`)

**New endpoints:**

- `POST /api/v1/operators` — create an operator
- `GET /api/v1/operators` — list all operators
- `GET /api/v1/operators/{id}` — get a single operator

**Changes to `FreightOrder`:**

- Remove `String orderedBy`
- Add `@ManyToOne(fetch = LAZY) Operator operator` (FK `operator_id`)
- `CreateFreightOrderRequest`: replace `orderedBy: String` with `operatorId: Long`
- `FreightOrderResponse`: expose `operatorId` and `operatorUsername`

**Migration:**

- Flyway `V2__add_operators.sql` — creates the `operators` table and adds the `operator_id` column
  to `freight_orders`
- If seed data contains existing `orderedBy` strings, the migration should insert corresponding
  `Operator` rows and back-fill the FK

**Hints:**

- This is a **breaking API change** — `orderedBy` is removed entirely, not deprecated alongside
  `operatorId`
- `Operator` will be the natural entity to link to an auth identity if authentication is introduced
  later; keep the model clean for that

**Acceptance criteria:**

- [ ] `Operator` entity, repository, service, and controller created
- [ ] `POST`, `GET (list)`, `GET (by id)` endpoints working under `/api/v1/operators`
- [ ] `FreightOrder.orderedBy` (String) removed; `operator` (`@ManyToOne`) added
- [ ] `CreateFreightOrderRequest` uses `operatorId: Long`
- [ ] `FreightOrderResponse` exposes `operatorId` and `operatorUsername`
- [ ] `V2__add_operators.sql` Flyway migration present
- [ ] `FreightOrderControllerTest` and `FreightOrderServiceTest` updated to use `operatorId`
- [ ] Existing seed data migrated in the SQL file if applicable
- [ ] Code is formatted

---

## CRG-001 — Bulk Cargo Support 🔴

**Labels:** `backend`, `business-logic`, `data-model`
**Depends on:** `INF-001`

The platform only supports containerised cargo (TEU-based). Bulk cargo — grain, ore, coal, crude
oil — is transported differently: measured in metric tonnes or cubic metres, loaded directly into
vessel holds, priced per tonne, and subject to deadweight tonnage (DWT) capacity rather than TEU
limits. There is no model for this today.

**Key differences from container cargo:**

| Dimension       | Container Cargo                        | Bulk Cargo                                                |
|-----------------|----------------------------------------|-----------------------------------------------------------|
| Unit            | TEU (20 ft / 40 ft)                    | Metric tonnes or m³                                       |
| Vessel capacity | TEU                                    | Deadweight tonnes (DWT)                                   |
| Identity        | ISO 6346 container code                | Commodity name + quantity                                 |
| Types           | Dry, Reefer, Open Top, Flat Rack, Tank | Dry bulk (grain, coal, ore); Liquid bulk (oil, chemicals) |
| Booking unit    | One container per order                | Quantity (tonnes) per commodity                           |

**New entity: `BulkCargo`**

- `id` — BIGINT PK
- `commodityName` — VARCHAR(100), NOT NULL (e.g. `"Wheat"`, `"Iron Ore"`, `"Crude Oil"`)
- `bulkCargoType` — ENUM(`DRY`, `LIQUID`)
- `quantityTonnes` — DECIMAL(12,2), NOT NULL
- `volumeCubicM` — DECIMAL(12,2), nullable (for liquids)
- `description` — VARCHAR(500), nullable

**New entity: `VoyageBulkRate`**

- `id` — BIGINT PK
- `voyage` — FK to `Voyage`
- `commodityType` — ENUM(`DRY`, `LIQUID`)
- `ratePerTonneUsd` — DECIMAL(12,4), NOT NULL
- Unique on (`voyage_id`, `commodity_type`)

**New endpoints:**

- `POST /api/v1/bulk-cargo` — register a bulk cargo shipment
- `GET /api/v1/bulk-cargo/{id}` — get bulk cargo details
- `GET /api/v1/voyages/{voyageId}/bulk-load` — DWT utilisation: tonnes used vs vessel DWT
- `GET /api/v1/voyages/{id}/bulk-rates` — list bulk rates for a voyage
- `POST /api/v1/voyages/{id}/bulk-rates` — set a bulk rate

**Changes to `FreightOrder`:**

- Add `cargoMode` — ENUM(`CONTAINER`, `BULK`); determines which association is active
- `container` association becomes nullable (null when `cargoMode = BULK`)
- Add optional `@ManyToOne BulkCargo bulkCargo` (null when `cargoMode = CONTAINER`)
- `FreightOrderResponse` includes bulk cargo fields when `cargoMode = BULK`

**Changes to `Vessel`:**

- Add optional `deadweightTonnage` — INT; required for voyages that carry bulk cargo

**Changes to `Voyage`:**

- Add `cargoMode` — ENUM(`CONTAINER`, `BULK`); a voyage carries one mode only
- Booking cutoff logic: bulk voyages check DWT utilisation, not TEU

**Migration:**

- `V3__bulk_cargo.sql` — new tables (`bulk_cargo`, `voyage_bulk_rates`), new columns
  (`freight_orders.cargo_mode`, `freight_orders.bulk_cargo_id`, `voyages.cargo_mode`,
  `vessels.deadweight_tonnage`)

**Hints:**

- Mixed container + bulk voyages are out of scope — a voyage has exactly one `cargoMode`
- `VoyagePrice` (TEU-based) is not extended; bulk pricing uses `VoyageBulkRate`
- AI price suggestion for bulk cargo is a separate follow-up ticket

**Acceptance criteria:**

- [ ] `BulkCargo` entity, repository, service, and controller created
- [ ] `VoyageBulkRate` entity, repository, and management endpoints under `/voyages/{id}/bulk-rates`
- [ ] `FreightOrder` extended with `cargoMode` and optional `bulkCargo` association
- [ ] `Vessel` extended with optional `deadweightTonnage`
- [ ] Booking validation: bulk orders check DWT capacity, not TEU
- [ ] `/voyages/{id}/bulk-load` endpoint returns DWT used and DWT available
- [ ] `FreightOrderResponse` includes bulk cargo fields when `cargoMode = BULK`
- [ ] `V3__bulk_cargo.sql` Flyway migration present
- [ ] Integration tests: create bulk order, DWT cutoff triggered, bulk order rejected on container
  voyage
- [ ] Existing container tests unaffected
- [ ] Code is formatted

---

## Dependency Graph

```
INF-001 (Flyway)  ──→  ENH-008 (Operator entity)  ──→  CRG-001 (Bulk Cargo)
                   └──→ CRG-001 (V3 migration)

ENH-006 (DTO ids)       — independent
ENH-007 (Error tests)   — independent (but easier after ENH-006 is merged)
```

## Suggested Order

1. **Start with** `ENH-006` — small, self-contained, good warm-up
2. **Then** `INF-001` — Flyway baseline must land before any further schema changes
3. **In parallel** `ENH-007` — no schema dependency; can run alongside INF-001
4. **Then** `ENH-008` — needs Flyway for `V2__add_operators.sql`
5. **Last** `CRG-001` — largest change; needs Flyway for `V3__bulk_cargo.sql`
