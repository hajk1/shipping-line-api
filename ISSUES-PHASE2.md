# Issues Backlog — Phase 2

Builds on top of Phase 1 issues (#1–8). These introduce pricing, invoicing, vessel planning, load
management, multi-ownership, and commission tracking.

## Issue Naming Convention

All issues follow the pattern: `DOMAIN-NNN — Title`

| Prefix | Domain                 |
|--------|------------------------|
| `PRC`  | Pricing & Discounts    |
| `INV`  | Invoicing & Email      |
| `CST`  | Customer Management    |
| `VPL`  | Vessel Planning & Load |
| `FIN`  | Finance & Ownership    |
| `AGT`  | Agents & Commissions   |

When referencing dependencies, use the full code (e.g. "Depends on `PRC-001`").

Issues are labeled by difficulty: 🟡 Medium | 🟠 Challenging | 🔴 Complex

---

## PRC-001 — Voyage Pricing Model 🟡

**Labels:** `backend`, `business-logic`, `pricing`
**Depends on:** Phase 1 Issue #4 (Voyage Controller)

Voyages should define a base freight price per container size. When a freight order is created, the
price is derived from the voyage pricing.

**New entity: `VoyagePrice`**

- `voyageId` (FK to Voyage)
- `containerSize` (TWENTY_FOOT / FORTY_FOOT)
- `basePriceUsd` (BigDecimal)
- Unique constraint on (`voyageId`, `containerSize`)

**Update `FreightOrder` entity:**

- Add `basePriceUsd` (BigDecimal) — copied from `VoyagePrice` at order creation time
- Add `discountPercent` (BigDecimal, default 0, range 0–100)
- Add `finalPriceUsd` (BigDecimal) — computed as `basePriceUsd * (1 - discountPercent / 100)`

**New endpoints:**

- `POST /api/v1/voyages/{voyageId}/prices` — set price for a container size on a voyage
- `GET /api/v1/voyages/{voyageId}/prices` — list prices for a voyage

**Updated behavior:**

- `FreightOrderService.createOrder()` should look up the `VoyagePrice` for the container's size and
  set `basePriceUsd` and `finalPriceUsd` on the order
- If no price is defined for that container size on the voyage, return 400 with a clear message

**Acceptance criteria:**

- [ ] Voyage prices can be set per container size
- [ ] Order creation auto-populates price from voyage
- [ ] `finalPriceUsd` is calculated correctly
- [ ] Order creation fails if voyage has no price for the container size
- [ ] Tests cover pricing lookup and missing price scenario
- [ ] Code is formatted

---

## PRC-002 — Discount Support on Freight Orders 🟡

**Labels:** `backend`, `business-logic`, `pricing`
**Depends on:** `PRC-001`

Allow setting or updating a discount on a freight order, both at creation and after.

**New endpoint:**

- `PATCH /api/v1/freight-orders/{id}/discount` — update discount on an existing order

**Request body:**

```json
{
  "discountPercent": 15.0,
  "reason": "Volume deal — Q2 campaign"
}
```

**Update `FreightOrder` entity:**

- Add `discountReason` (String, nullable, max 500 chars)

**Business rules:**

- Discount can be set at order creation (optional field in `CreateFreightOrderRequest`) or later via
  PATCH
- `discountPercent` must be between 0 and 100
- `finalPriceUsd` must be recalculated whenever discount changes
- Discount cannot be changed on `CANCELLED` or `DELIVERED` orders (return 409)

**Acceptance criteria:**

- [ ] Discount can be applied at creation and updated later
- [ ] `finalPriceUsd` recalculates correctly on every discount change
- [ ] Validation enforces 0–100 range
- [ ] Cannot modify discount on terminal-status orders
- [ ] Tests cover creation with discount, PATCH update, and blocked update on cancelled order
- [ ] Code is formatted

---

## CST-001 — Customer Entity 🟡

**Labels:** `backend`, `data-model`

Before we can send invoices, we need to know who the customer is. Create a `Customer` entity and
link freight orders to it.

**New entity: `Customer`**

- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `companyName` (String, required)
- `contactName` (String, required)
- `email` (String, required, valid email format)
- `phone` (String, optional)
- `address` (String, optional)

**New endpoints:**

- `POST /api/v1/customers` — create
- `GET /api/v1/customers` — list
- `GET /api/v1/customers/{id}` — get by ID

**Update `FreightOrder`:**

- Add `customerId` (FK to Customer, required)
- Update `CreateFreightOrderRequest` to include `customerId`
- Update `FreightOrderResponse` to include `customerName` and `customerEmail`

**Acceptance criteria:**

- [ ] Customer CRUD works
- [ ] Email validation enforced
- [ ] Freight orders require a valid customer
- [ ] Existing tests updated to include a customer
- [ ] Code is formatted

---

## INV-001 — Generate Invoice PDF for Finalized Orders 🟠

**Labels:** `backend`, `business-logic`, `invoicing`
**Depends on:** `PRC-001`, `PRC-002`, `CST-001`

When a freight order is marked as `DELIVERED`, generate a PDF invoice and allow it to be downloaded.

**New endpoint:**

- `GET /api/v1/freight-orders/{id}/invoice` — returns PDF (Content-Type: `application/pdf`)

**Invoice should include:**

- Invoice number (auto-generated, e.g. `INV-2025-00042`)
- Customer name, email, address
- Voyage number, departure/arrival ports
- Container code, size, type
- Base price, discount %, discount reason, final price
- Order date and delivery date
- Company footer (hardcode for now)

**Technical hints:**

- Use a PDF library — `iText` (AGPL) or `OpenPDF` (LGPL, recommended for POC)
- Add dependency to `pom.xml`
- Create an `InvoiceService` that builds the PDF as a `byte[]`
- Controller returns `ResponseEntity<byte[]>` with proper headers

**Business rules:**

- Invoice can only be generated for orders with status `DELIVERED`
- Return 409 if order is not yet delivered

**Acceptance criteria:**

- [ ] PDF downloads for delivered orders
- [ ] PDF contains all required fields
- [ ] Returns 409 for non-delivered orders
- [ ] At least one test verifying PDF generation returns 200 with correct content type
- [ ] Code is formatted

---

## INV-002 — Email Invoice to Customer 🟠

**Labels:** `backend`, `business-logic`, `invoicing`
**Depends on:** `CST-001`, `INV-001`

Add ability to email the invoice PDF to the customer.

**New endpoint:**

- `POST /api/v1/freight-orders/{id}/invoice/send` — generates and emails the invoice

**Setup:**

- Add `spring-boot-starter-mail` dependency
- Configure SMTP in `application.properties` (use environment variables for credentials)
- For local dev, document how to use [MailHog](https://github.com/mailhog/MailHog)
  or [Mailtrap](https://mailtrap.io/)

**What to create:**

- `EmailService` — generic email sender (to, subject, body, attachment)
- Wire it into `InvoiceService` or create an `InvoiceEmailService`

**Business rules:**

- Can only send invoice for `DELIVERED` orders
- Email goes to the customer's email address from the `Customer` entity
- Subject: `Invoice INV-2025-XXXXX — Voyage VOY-XXXX`
- Body: brief text with order summary, PDF as attachment

**Acceptance criteria:**

- [ ] Email sends with PDF attachment for delivered orders
- [ ] Returns 409 for non-delivered orders
- [ ] README updated with SMTP / MailHog setup instructions
- [ ] Test with mocked `JavaMailSender` verifies email is triggered
- [ ] Code is formatted

---

## VPL-001 — Voyage Load Tracking and Manual Booking Stop 🟠

**Labels:** `backend`, `business-logic`, `vessel-planning`
**Depends on:** Phase 1 Issue #4 (Voyage Controller)

Track the current load on a voyage (in TEU) and allow ops to manually stop accepting new orders.

**Update `Voyage` entity:**

- Add `maxCapacityTeu` (int) — defaults from `vessel.capacityTeu` at creation but can be overridden
- Add `bookingOpen` (boolean, default `true`)

**New endpoints:**

- `GET /api/v1/voyages/{id}/load` — returns current load summary
- `PATCH /api/v1/voyages/{id}/booking-status` — manually open or close bookings

**Load summary response:**

```json
{
  "voyageNumber": "VOY-2025-001",
  "maxCapacityTeu": 5000,
  "currentLoadTeu": 1240,
  "utilizationPercent": 24.8,
  "bookingOpen": true,
  "containerCount": 800
}
```

**TEU calculation:**

- TWENTY_FOOT = 1 TEU
- FORTY_FOOT = 2 TEU
- Only count orders with status `PENDING`, `CONFIRMED`, or `IN_TRANSIT`

**Business rules:**

- When `bookingOpen` is `false`, `FreightOrderService.createOrder()` must reject new orders (return
  409)
- Manual toggle via PATCH overrides everything

**Acceptance criteria:**

- [ ] Load endpoint returns correct TEU count and utilization
- [ ] Ops can manually close/open bookings
- [ ] Closed voyages reject new freight orders with 409
- [ ] Tests cover load calculation and booking block
- [ ] Code is formatted

---

## VPL-002 — Automatic Booking Cutoff Based on Capacity 🔴

**Labels:** `backend`, `business-logic`, `vessel-planning`
**Depends on:** `VPL-001`

Automatically close bookings when a voyage reaches a configurable capacity threshold.

**Configuration:**

- Add `app.booking.auto-cutoff-percent` to `application.properties` (default: 95)

**Behavior:**

- After every successful `createOrder()`, check current load vs `maxCapacityTeu`
- If `currentLoadTeu / maxCapacityTeu >= threshold`, automatically set `bookingOpen = false`
- Log a warning when auto-cutoff triggers
- Manual reopen via the PATCH endpoint from `VPL-001` should still work

**Edge case:**

- If a 40ft container would exceed capacity but a 20ft would not, the order should be rejected with
  a clear message explaining remaining TEU

**Acceptance criteria:**

- [ ] Auto-cutoff triggers at the configured threshold
- [ ] Orders that would exceed remaining TEU are rejected
- [ ] Manual reopen still works after auto-cutoff
- [ ] Threshold is configurable via properties
- [ ] Tests cover threshold trigger and the edge case
- [ ] Code is formatted

---

## FIN-001 — Vessel Ownership Model 🟠

**Labels:** `backend`, `data-model`, `finance`

A vessel can have multiple owners with different ownership shares. This is needed for cost/profit
splitting after a voyage.

**New entity: `VesselOwner`**

- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `vesselId` (FK to Vessel)
- `ownerName` (String, required)
- `ownerEmail` (String, required)
- `sharePercent` (BigDecimal, required, range 0.01–100)

**Constraint:** The sum of all `sharePercent` values for a vessel must not exceed 100.

**New endpoints:**

- `POST /api/v1/vessels/{vesselId}/owners` — add an owner
- `GET /api/v1/vessels/{vesselId}/owners` — list owners with shares
- `DELETE /api/v1/vessels/{vesselId}/owners/{ownerId}` — remove an owner

**Validation rules:**

- Adding an owner that would push total above 100% returns 409
- `sharePercent` must be greater than 0

**Acceptance criteria:**

- [ ] Multiple owners can be added per vessel
- [ ] Share total cannot exceed 100%
- [ ] Owners can be listed and removed
- [ ] Tests cover happy path and exceeding 100%
- [ ] Code is formatted

---

## FIN-002 — Voyage Financial Summary with Owner Profit Split 🔴

**Labels:** `backend`, `business-logic`, `finance`
**Depends on:** `PRC-001`, `FIN-001`

After a voyage is `COMPLETED`, generate a financial summary showing revenue, costs, and profit split
per vessel owner.

**New entity: `VoyageCost`**

- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `voyageId` (FK to Voyage)
- `description` (String — e.g. "Fuel", "Port fees", "Crew")
- `amountUsd` (BigDecimal)

**New endpoints:**

- `POST /api/v1/voyages/{voyageId}/costs` — add a cost line item
- `GET /api/v1/voyages/{voyageId}/costs` — list cost items
- `GET /api/v1/voyages/{voyageId}/financial-summary` — full breakdown

**Financial summary response:**

```json
{
  "voyageNumber": "VOY-2025-001",
  "totalRevenueUsd": 250000.00,
  "totalCostsUsd": 180000.00,
  "netProfitUsd": 70000.00,
  "orderCount": 120,
  "owners": [
    {
      "ownerName": "Alpha Shipping Ltd",
      "sharePercent": 60.0,
      "revenueShareUsd": 150000.00,
      "costShareUsd": 108000.00,
      "profitShareUsd": 42000.00
    },
    {
      "ownerName": "Beta Maritime Co",
      "sharePercent": 40.0,
      "revenueShareUsd": 100000.00,
      "costShareUsd": 72000.00,
      "profitShareUsd": 28000.00
    }
  ]
}
```

**Business rules:**

- Revenue = sum of `finalPriceUsd` from all `DELIVERED` freight orders on the voyage
- Costs = sum of all `VoyageCost` items
- Profit = Revenue - Costs
- Each owner's share = their `sharePercent` applied to revenue, cost, and profit
- Summary can only be generated for `COMPLETED` voyages (return 409 otherwise)

**Acceptance criteria:**

- [ ] Costs can be added and listed
- [ ] Financial summary calculates correctly
- [ ] Profit splits match owner share percentages
- [ ] Returns 409 for non-completed voyages
- [ ] Tests cover multi-owner split with sample data
- [ ] Code is formatted

---

## AGT-001 — Agent / Freight Forwarder Entity 🟠

**Labels:** `backend`, `data-model`, `commission`

Replace the free-text `orderedBy` field with a proper `Agent` entity to track internal staff and
future external freight forwarders.

**New entity: `Agent`**

- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `name` (String, required)
- `email` (String, required)
- `commissionPercent` (BigDecimal, required, range 0–100)
- `type` (enum: `INTERNAL`, `EXTERNAL`)
- `active` (boolean, default true)

**New endpoints:**

- `POST /api/v1/agents` — create
- `GET /api/v1/agents` — list (optional filter by `type` and `active`)
- `GET /api/v1/agents/{id}` — get by ID
- `PATCH /api/v1/agents/{id}` — update commission rate or active status

**Update `FreightOrder`:**

- Replace `orderedBy` (String) with `agentId` (FK to Agent)
- Update DTOs and existing tests accordingly

**Migration note:** This is a breaking change to `FreightOrder`. Add a data migration note in the PR
description. For the POC, dropping and recreating is fine — document it.

**Acceptance criteria:**

- [ ] Agent CRUD works with type and active filters
- [ ] Freight orders now reference an agent
- [ ] Existing `FreightOrderController` and tests updated
- [ ] `orderedBy` field removed
- [ ] Code is formatted

---

## AGT-002 — Commission Calculation per Agent After Voyage 🔴

**Labels:** `backend`, `business-logic`, `commission`
**Depends on:** `PRC-001`, `AGT-001`

After a voyage is `COMPLETED`, calculate each agent's commission from the orders they placed.

**New endpoint:**

- `GET /api/v1/voyages/{voyageId}/commissions` — agent commission breakdown

**Commission report response:**

```json
{
  "voyageNumber": "VOY-2025-001",
  "agents": [
    {
      "agentName": "Ali Hassan",
      "type": "INTERNAL",
      "commissionPercent": 5.0,
      "orderCount": 12,
      "totalOrderValueUsd": 48000.00,
      "commissionEarnedUsd": 2400.00
    },
    {
      "agentName": "FastFreight FZE",
      "type": "EXTERNAL",
      "commissionPercent": 8.0,
      "orderCount": 6,
      "totalOrderValueUsd": 30000.00,
      "commissionEarnedUsd": 2400.00
    }
  ],
  "totalCommissionsUsd": 4800.00
}
```

**Business rules:**

- Commission = `agent.commissionPercent` × sum of `finalPriceUsd` for that agent's `DELIVERED`
  orders on the voyage
- Only `DELIVERED` orders count
- Only `COMPLETED` voyages (return 409 otherwise)

**Acceptance criteria:**

- [ ] Commission calculates correctly per agent
- [ ] Mixed internal/external agents handled
- [ ] Returns 409 for non-completed voyages
- [ ] Tests with multiple agents and orders
- [ ] Code is formatted

---

## AGT-003 — Email Commission Report to Agents 🔴

**Labels:** `backend`, `business-logic`, `commission`, `invoicing`
**Depends on:** `INV-002`, `AGT-002`

Add ability to email each agent their commission statement after a voyage completes.

**New endpoint:**

- `POST /api/v1/voyages/{voyageId}/commissions/send` — calculates and emails each agent

**Behavior:**

- For each agent with orders on the voyage, generate a summary and email it
- Email subject: `Commission Statement — Voyage VOY-XXXX`
- Body: text summary of their orders, total value, commission earned
- Optional: attach a PDF (reuse PDF generation pattern from `INV-001`)

**Business rules:**

- Only for `COMPLETED` voyages
- Only sends to active agents
- Returns a summary of how many emails were sent

**Response:**

```json
{
  "voyageNumber": "VOY-2025-001",
  "emailsSent": 3,
  "totalCommissionsUsd": 7200.00
}
```

**Acceptance criteria:**

- [ ] Emails sent to each active agent with orders
- [ ] Inactive agents are skipped
- [ ] Returns 409 for non-completed voyages
- [ ] Test with mocked `JavaMailSender`
- [ ] Code is formatted

---

## Dependency Graph

```
PRC-001 (Voyage Pricing)
  ├──→ PRC-002 (Discounts)
  ├──→ INV-001 (Invoice PDF)
  ├──→ FIN-002 (Financial Summary)
  └──→ AGT-002 (Commission Calc)

CST-001 (Customer Entity)
  ├──→ INV-001 (Invoice PDF)
  └──→ INV-002 (Email Invoice)

INV-001 (Invoice PDF)
  └──→ INV-002 (Email Invoice)
       └──→ AGT-003 (Email Commission)

VPL-001 (Load Tracking)
  └──→ VPL-002 (Auto Cutoff)

FIN-001 (Vessel Ownership)
  └──→ FIN-002 (Financial Summary)

AGT-001 (Agent Entity)
  └──→ AGT-002 (Commission Calc)
       └──→ AGT-003 (Email Commission)
```

## Suggested Team Allocation

| Track | Issues                                        | Can start when                  |
|-------|-----------------------------------------------|---------------------------------|
| A     | `PRC-001` → `PRC-002` → `INV-001` → `INV-002` | Phase 1 complete                |
| B     | `VPL-001` → `VPL-002`                         | Phase 1 Issue #4 complete       |
| C     | `FIN-001` → `FIN-002`                         | `PRC-001` + `FIN-001` complete  |
| D     | `AGT-001` → `AGT-002` → `AGT-003`             | `PRC-001` + `AGT-001` complete  |
| —     | `CST-001`                                     | Anytime (no Phase 2 dependency) |

Tracks A and B can run fully in parallel. Tracks C and D can start their first issue immediately but
need `PRC-001` before the calculation issues.