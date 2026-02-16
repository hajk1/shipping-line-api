# Issues Backlog — Phase 1

Core CRUD controllers and cross-cutting features to complete the boilerplate.

## Issue Naming Convention

All issues follow the pattern: `DOMAIN-NNN — Title`

| Prefix | Domain                       |
|--------|------------------------------|
| `CRD`  | CRUD Controllers             |
| `ENH`  | Enhancements & Cross-cutting |

Issues are labeled by difficulty: 🟢 Easy | 🟡 Medium | 🟠 Challenging

---

## CRD-001 — Port CRUD Controller 🟢

**Labels:** `good-first-issue`, `backend`

Build a REST controller for managing ports.

**Endpoints:**
- `POST /api/v1/ports` — create a port
- `GET /api/v1/ports` — list all ports
- `GET /api/v1/ports/{id}` — get a single port

**What to create:**
- `CreatePortRequest` DTO (fields: `unlocode`, `name`, `country`)
- `PortResponse` DTO with `fromEntity()` factory method
- `PortService`
- `PortController`
- `PortControllerTest` — at least 2 tests (create + list)

**Validation rules:**
- `unlocode` must be exactly 5 characters and unique
- `name` and `country` are required

**Reference:** Follow the same pattern as `FreightOrderController`.

**Acceptance criteria:**
- [ ] All endpoints return correct HTTP status codes (201, 200, 404)
- [ ] Duplicate `unlocode` returns 409 Conflict
- [ ] Tests pass with `mvn test`
- [ ] Code is formatted (`mvn fmt:check` passes)

---

## CRD-002 — Vessel CRUD Controller 🟢

**Labels:** `good-first-issue`, `backend`

Build a REST controller for managing vessels.

**Endpoints:**
- `POST /api/v1/vessels` — create a vessel
- `GET /api/v1/vessels` — list all vessels
- `GET /api/v1/vessels/{id}` — get a single vessel

**What to create:**
- `CreateVesselRequest` DTO (fields: `name`, `imoNumber`, `capacityTeu`)
- `VesselResponse` DTO with `fromEntity()` factory method
- `VesselService`
- `VesselController`
- `VesselControllerTest` — at least 2 tests

**Validation rules:**
- `imoNumber` must be exactly 7 characters and unique
- `capacityTeu` must be a positive number
- `name` is required

**Acceptance criteria:**
- [ ] All endpoints return correct HTTP status codes
- [ ] Duplicate `imoNumber` returns 409 Conflict
- [ ] Tests pass with `mvn test`
- [ ] Code is formatted

---

## CRD-003 — Container CRUD Controller 🟢

**Labels:** `good-first-issue`, `backend`

Build a REST controller for managing containers.

**Endpoints:**
- `POST /api/v1/containers` — create a container
- `GET /api/v1/containers` — list all containers (optional filter by `size` or `type`)
- `GET /api/v1/containers/{id}` — get a single container

**What to create:**
- `CreateContainerRequest` DTO (fields: `containerCode`, `size`, `type`)
- `ContainerResponse` DTO with `fromEntity()` factory method
- `ContainerService`
- `ContainerController`
- `ContainerControllerTest` — at least 2 tests

**Validation rules:**
- `containerCode` must be exactly 11 characters and unique
- `size` must be a valid `ContainerSize` enum value
- `type` must be a valid `ContainerType` enum value

**Hint:** For the optional filter, look at how `FreightOrderController.list()` handles the optional `voyageId` parameter.

**Acceptance criteria:**
- [ ] All endpoints return correct HTTP status codes
- [ ] Invalid enum values return 400 Bad Request
- [ ] Tests pass and code is formatted

---

## CRD-004 — Voyage Controller with Business Logic 🟡

**Labels:** `backend`, `business-logic`

Build a REST controller for managing voyages. This one has more business logic than the other CRUDs.

**Endpoints:**
- `POST /api/v1/voyages` — create a voyage
- `GET /api/v1/voyages` — list all voyages (optional filter by `status`)
- `GET /api/v1/voyages/{id}` — get a single voyage
- `PATCH /api/v1/voyages/{id}/status` — update voyage status

**What to create:**
- `CreateVoyageRequest` DTO (fields: `voyageNumber`, `vesselId`, `departurePortId`, `arrivalPortId`, `departureTime`, `arrivalTime`)
- `UpdateVoyageStatusRequest` DTO (field: `status`)
- `VoyageResponse` DTO — should include vessel name, departure/arrival port names (not just IDs)
- `VoyageService`
- `VoyageController`
- `VoyageControllerTest` — at least 3 tests

**Validation rules:**
- `departureTime` must be in the future
- `arrivalTime` must be after `departureTime`
- `departurePortId` and `arrivalPortId` must be different
- `voyageNumber` must be unique

**Status transition rules:**
- `PLANNED` → `IN_PROGRESS` or `CANCELLED`
- `IN_PROGRESS` → `COMPLETED` or `CANCELLED`
- `COMPLETED` and `CANCELLED` are final (no further transitions)
- Invalid transitions should return 409 Conflict

**Acceptance criteria:**
- [ ] All validation rules enforced
- [ ] Status transitions follow the rules above
- [ ] Response includes readable port names and vessel name
- [ ] Tests cover happy path + at least one invalid transition
- [ ] Code is formatted

---

## ENH-001 — Pagination on List Endpoints 🟡

**Labels:** `backend`, `enhancement`

All list endpoints currently return everything. Add pagination support using Spring Data's `Pageable`.

**Scope:** Update `GET /api/v1/freight-orders` as the first example, then apply the same pattern to the other list endpoints.

**Expected query parameters:**
- `page` — page number (0-based, default 0)
- `size` — items per page (default 20, max 100)
- `sort` — sort field and direction (e.g. `createdAt,desc`)

**Expected response shape:**
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

**Hints:**
- Change repository methods to return `Page<T>` instead of `List<T>`
- Accept `Pageable` in the controller method — Spring auto-binds the query params
- Create a generic `PageResponse<T>` wrapper DTO

**Acceptance criteria:**
- [ ] Pagination works on `FreightOrderController.list()`
- [ ] Default page size is 20, max is 100
- [ ] Response includes `totalElements` and `totalPages`
- [ ] Add a test that creates multiple orders and verifies paging
- [ ] Code is formatted

---

## ENH-002 — Swagger / OpenAPI Documentation 🟡

**Labels:** `backend`, `documentation`

Add auto-generated API docs so the team can explore endpoints in a browser.

**Steps:**
1. Add `springdoc-openapi-starter-webmvc-ui` dependency to `pom.xml`
2. Verify Swagger UI loads at `http://localhost:8080/swagger-ui.html`
3. Add `@Operation` and `@ApiResponse` annotations to `FreightOrderController` as a reference
4. Add a brief `@OpenAPIDefinition` on the main application class with title, version, and description

**Do NOT annotate every controller** — just annotate `FreightOrderController` as an example for others to follow.

**Acceptance criteria:**
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] `FreightOrderController` endpoints show descriptions and response codes
- [ ] Other controllers still appear (auto-detected) but without custom annotations
- [ ] Update `README.md` with a note about Swagger UI
- [ ] Code is formatted

---

## ENH-003 — List Containers on a Voyage 🟠

**Labels:** `backend`, `business-logic`
**Depends on:** `CRD-004`

Add an endpoint to see all containers booked on a specific voyage.

**Endpoint:**
- `GET /api/v1/voyages/{voyageId}/containers` — list all containers assigned to a voyage via freight orders

**Expected response:** A list of objects with container details + order info:
```json
[
  {
    "containerCode": "MSCU1234567",
    "size": "TWENTY_FOOT",
    "type": "DRY",
    "orderedBy": "ops-team",
    "orderStatus": "PENDING"
  }
]
```

**Hints:**
- You can query through `FreightOrderRepository.findByVoyageId()` and map the results
- Create a `VoyageContainerResponse` DTO
- This could live in `VoyageController` or `FreightOrderController` — pick what feels right and justify in your PR

**Acceptance criteria:**
- [ ] Endpoint returns containers for a valid voyage
- [ ] Empty list for a voyage with no bookings
- [ ] 404 if voyage doesn't exist
- [ ] At least one integration test
- [ ] Code is formatted

---

## ENH-004 — Prevent Double-Booking a Container 🟠

**Labels:** `backend`, `business-logic`, `bug-prevention`

Currently nothing stops the same container from being booked on overlapping voyages. Add a validation rule.

**Rule:** A container cannot be assigned to two voyages whose time ranges overlap. Specifically, if a container is already booked on a voyage with `departureTime` → `arrivalTime`, it cannot be booked on another voyage that overlaps that time window.

**Where to add this:** `FreightOrderService.createOrder()` — before saving.

**Hints:**
- Add a custom query in `FreightOrderRepository` (or `VoyageRepository`) to check for overlapping voyages for a given container
- Consider only `PENDING`, `CONFIRMED`, and `IN_TRANSIT` orders (ignore `CANCELLED` and `DELIVERED`)
- Return 409 Conflict with a clear message if there's an overlap

**Acceptance criteria:**
- [ ] Cannot book a container on overlapping voyages
- [ ] Cancelled/delivered orders don't block new bookings
- [ ] Returns 409 with a descriptive error message
- [ ] At least two tests: one blocked, one allowed after cancellation
- [ ] Code is formatted

---

## Dependency Graph

```
CRD-001 (Port)         ─┐
CRD-002 (Vessel)        ├──→ CRD-004 (Voyage) ──→ ENH-003 (Voyage Containers)
CRD-003 (Container)    ─┘

ENH-001 (Pagination)        — independent
ENH-002 (Swagger)            — independent
ENH-004 (Double-Booking)     — independent
```

## Suggested Order

1. **Start with** `CRD-001`, `CRD-002`, `CRD-003` — independent, pick any
2. **Then** `CRD-004` — needs ports and vessels to exist
3. **In parallel** `ENH-001`, `ENH-002`, `ENH-004` — can be done anytime
4. **Last** `ENH-003` — needs `CRD-004` done first