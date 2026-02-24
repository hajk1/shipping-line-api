# Issues Backlog — Phase 2

Builds on top of Phase 1 issues. These introduce pricing, invoicing, vessel planning, load
management, multi-ownership, commission tracking, and barcode-based shipment tracking.

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
| `TRK`  | Tracking & Barcodes    |
| `NTF`  | Notifications & Alerts |
| `AIP`  | AI-Powered Pricing     |

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

---

## NTF-001 — Notification Template Engine 🟡

**Labels:** `backend`, `infrastructure`, `notifications`
**Depends on:** `INV-002` (reuses `EmailService`)

Build a reusable notification engine that sends templated emails triggered by business events. This
is the foundation for all customer-facing communications.

**New entity: `NotificationTemplate`**

- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `code` (String, unique — e.g. `BOOKING_CONFIRMED`, `ARRIVAL_NOTICE`)
- `subject` (String — supports placeholders like `{voyageNumber}`)
- `body` (String, TEXT column — HTML email body with placeholders)
- `active` (boolean, default true)

**New entity: `NotificationLog`**

- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `templateCode` (String)
- `freightOrderId` (FK to FreightOrder, nullable)
- `recipientEmail` (String)
- `subject` (String — rendered, not template)
- `status` (enum: `SENT`, `FAILED`)
- `errorMessage` (String, nullable)
- `sentAt` (LocalDateTime)

**What to create:**

**`NotificationService`:**

```java
public void send(String templateCode, Long freightOrderId, Map<String, String> variables);
```

- Looks up template by code
- Replaces placeholders in subject and body with provided variables
- Sends email via `EmailService` (from `INV-002`)
- Logs result to `NotificationLog`

**Placeholder replacement:** Simple `{key}` → value string replacement. No need for a full template
engine in the POC.

**Common variables** (available to all templates):

- `{customerName}`, `{customerEmail}`, `{companyName}`
- `{containerCode}`, `{containerSize}`, `{containerType}`
- `{voyageNumber}`, `{vesselName}`
- `{departurePort}`, `{arrivalPort}`
- `{departureTime}`, `{arrivalTime}`
- `{orderStatus}`, `{orderId}`

**New endpoints:**

- `POST /api/v1/notifications/templates` — create a template
- `GET /api/v1/notifications/templates` — list all templates
- `PUT /api/v1/notifications/templates/{code}` — update a template
- `GET /api/v1/notifications/log?orderId={id}` — view notification history for an order

**Seed data** — insert default templates via `data.sql`:

- `BOOKING_CONFIRMED`
- `DEPARTURE_NOTICE`
- `ARRIVAL_NOTICE`
- `DELIVERY_CONFIRMATION`
- `BOOKING_CANCELLED`
- `VOYAGE_DELAYED`

**Acceptance criteria:**

- [ ] Templates stored in DB with placeholder support
- [ ] `NotificationService.send()` renders and emails correctly
- [ ] Sent/failed attempts logged in `NotificationLog`
- [ ] Template CRUD endpoints work
- [ ] Notification log queryable by order ID
- [ ] Test with mocked `EmailService` verifies rendering and logging
- [ ] Code is formatted

---

## NTF-002 — Booking Confirmation and Cancellation Notices 🟡

**Labels:** `backend`, `business-logic`, `notifications`
**Depends on:** `NTF-001`, `CST-001`

Automatically email the customer when their freight order is confirmed or cancelled.

**Trigger points — update `FreightOrderService`:**

- When order status changes to `CONFIRMED` → send `BOOKING_CONFIRMED` template
- When order status changes to `CANCELLED` → send `BOOKING_CANCELLED` template

**Booking confirmation email content:**

- Subject: `Booking Confirmed — {containerCode} on Voyage {voyageNumber}`
- Body: customer greeting, container details, voyage details (vessel, route, departure/arrival
  dates), order ID for reference, note to contact ops for changes

**Cancellation email content:**

- Subject: `Booking Cancelled — {containerCode} on Voyage {voyageNumber}`
- Body: customer greeting, confirmation that booking has been cancelled, container and voyage
  reference, note to contact ops to rebook

**Business rules:**

- Only send if the customer has a valid email address
- If email fails, log the failure but do NOT roll back the status change
- Do not send notifications for orders that skip directly to a terminal status (e.g. bulk imports)

**Acceptance criteria:**

- [ ] Confirmation email sent automatically on status → `CONFIRMED`
- [ ] Cancellation email sent automatically on status → `CANCELLED`
- [ ] Email failure does not block the status transition
- [ ] Notifications logged in `NotificationLog`
- [ ] Test verifies notification triggered on status change
- [ ] Code is formatted

---

## NTF-003 — Departure Notice 🟡

**Labels:** `backend`, `business-logic`, `notifications`
**Depends on:** `NTF-001`, `CST-001`, `CRD-004`

Notify customers when their cargo has departed.

**Trigger point — update `VoyageService`:**

- When voyage status changes to `IN_PROGRESS` → send `DEPARTURE_NOTICE` to ALL customers with active
  orders on that voyage

**Departure notice email content:**

- Subject: `Departure Notice — Voyage {voyageNumber} from {departurePort}`
- Body: customer greeting, confirmation that vessel has departed, vessel name, departure port and
  time, estimated arrival port and time, container code and order reference, tracking link (if
  `TRK-002` is done: `{baseUrl}/api/v1/track/order/{orderId}`)

**What to create:**

- `VoyageNotificationService` — queries all active freight orders for a voyage, sends departure
  notice to each customer
- Handles deduplication: if a customer has multiple containers on the same voyage, send ONE email
  listing all their containers (not one per container)

**Business rules:**

- Only notify for orders with status `CONFIRMED` or `IN_TRANSIT`
- Skip customers without email
- Email failures logged per order but do not block voyage status change
- Include tracking URL only if it's configured (`app.base-url` is set)

**Acceptance criteria:**

- [ ] All customers with active orders notified on voyage departure
- [ ] Multi-container customers receive a single consolidated email
- [ ] Email failure does not block voyage transition
- [ ] Notifications logged per order
- [ ] Test with 2+ customers on a voyage verifies correct emails sent
- [ ] Code is formatted

---

## NTF-004 — Arrival Notice (Advance) 🟠

**Labels:** `backend`, `business-logic`, `notifications`
**Depends on:** `NTF-001`, `CST-001`, `CRD-004`

Send an advance arrival notice to customers X days before the vessel's estimated arrival. This is
the most operationally important notification — customers need it to arrange customs, trucking, and
warehouse.

**Configuration:**

```properties
app.notification.arrival-notice-days-before=3
```

**What to create:**

**`ArrivalNoticeScheduler`** — a Spring `@Scheduled` job:

- Runs daily (e.g. every day at 06:00 UTC)
- Queries voyages where `status = IN_PROGRESS` and `arrivalTime` is within the configured days
  window
- For each matching voyage, sends `ARRIVAL_NOTICE` to all customers with active orders
- Tracks which orders have already received an arrival notice (to avoid duplicates)

**Update `FreightOrder` entity:**

- Add `arrivalNoticeSent` (boolean, default `false`)

**Arrival notice email content:**

- Subject: `Arrival Notice — {containerCode} arriving at {arrivalPort}`
- Body: customer greeting, estimated arrival date/time, vessel and voyage details, container
  details, reminder to arrange customs clearance and transport, tracking link, ops contact info

**Consolidated emails:** Same dedup logic as `NTF-003` — one email per customer per voyage listing
all their containers.

**New endpoint (manual trigger for ops):**

- `POST /api/v1/voyages/{voyageId}/send-arrival-notice` — manually send arrival notices for a voyage
  regardless of timing (useful if schedule changes)

**Business rules:**

- Automatic: only sent once per order (`arrivalNoticeSent` flag)
- Manual trigger: always sends, resets `arrivalNoticeSent` flag (covers re-notification after
  schedule change)
- Only for `IN_PROGRESS` voyages
- Scheduler should be resilient — if one email fails, continue with the rest

**Acceptance criteria:**

- [ ] Scheduler sends notices within configured window
- [ ] Each order only auto-notified once
- [ ] Manual trigger endpoint works for ops override
- [ ] Consolidated emails for multi-container customers
- [ ] Scheduler resilient to individual email failures
- [ ] Test with mocked clock verifies scheduler timing logic
- [ ] Code is formatted

---

## NTF-005 — Delivery Confirmation Notice 🟡

**Labels:** `backend`, `business-logic`, `notifications`
**Depends on:** `NTF-001`, `CST-001`

Notify the customer when their freight order is marked as delivered.

**Trigger point — update `FreightOrderService`:**

- When order status changes to `DELIVERED` → send `DELIVERY_CONFIRMATION` template

**Delivery confirmation email content:**

- Subject: `Delivery Confirmation — {containerCode} from Voyage {voyageNumber}`
- Body: customer greeting, confirmation of delivery, container and voyage details, final price
  summary (if `PRC-001` is done: base price, discount, final price), note that invoice will follow (
  or is attached if `INV-001` is done), thank you message

**Optional integration with `INV-001`:**

- If invoice generation is available, attach the invoice PDF to the delivery confirmation email
- If not available, just send the text confirmation

**Business rules:**

- Only send on transition to `DELIVERED`
- Email failure does not block status change
- Logged in `NotificationLog`

**Acceptance criteria:**

- [ ] Delivery confirmation sent on status → `DELIVERED`
- [ ] Invoice attached if `INV-001` is available (graceful if not)
- [ ] Email failure does not block status transition
- [ ] Test verifies notification triggered
- [ ] Code is formatted

---

## NTF-006 — Voyage Delay Alert 🟠

**Labels:** `backend`, `business-logic`, `notifications`
**Depends on:** `NTF-001`, `CST-001`, `CRD-004`

Notify customers when a voyage's arrival time is updated after departure, indicating a delay.

**Trigger point — new `VoyageService` method:**

- `updateArrivalTime(Long voyageId, LocalDateTime newArrivalTime, String reason)`
- Only applicable for voyages with status `IN_PROGRESS`
- If new arrival time is later than the original, trigger `VOYAGE_DELAYED` notification to all
  affected customers

**New endpoint:**

- `PATCH /api/v1/voyages/{voyageId}/arrival-time`

**Request body:**

```json
{
  "newArrivalTime": "2025-04-18T14:00:00",
  "reason": "Vessel rerouted via Cape of Good Hope due to security advisory"
}
```

**Delay alert email content:**

- Subject: `Schedule Update — Voyage {voyageNumber} to {arrivalPort}`
- Body: customer greeting, acknowledgment of delay, original arrival time vs new arrival time, delay
  reason, updated container and voyage details, apology and ops contact info

**Consolidated emails:** One email per customer per voyage.

**Update `Voyage` entity:**

- Add `originalArrivalTime` (LocalDateTime, nullable) — set on first delay, preserves the original
  ETA

**Business rules:**

- Only send if new arrival is LATER than current (early arrival doesn't need an alert)
- Only for `IN_PROGRESS` voyages (return 409 otherwise)
- If arrival is updated multiple times, each update triggers a new notification (with updated times)
- Reset `arrivalNoticeSent` flag from `NTF-004` so a new arrival notice is sent at the correct time

**Acceptance criteria:**

- [ ] Delay alert sent when arrival time pushed later
- [ ] No alert when arrival time moved earlier
- [ ] Returns 409 for non-in-progress voyages
- [ ] `originalArrivalTime` preserved from first update
- [ ] `arrivalNoticeSent` reset to allow re-notification
- [ ] Consolidated emails for multi-container customers
- [ ] Test covers delay scenario and early-arrival no-op
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

## TRK-001 — Barcode and QR Code Generation Service 🟡

**Labels:** `backend`, `enhancement`, `tracking`

Add a reusable service that generates Code 128 barcodes and QR codes as PNG byte arrays. This is the
foundation for all tracking and label features.

**Setup:**
- Add ZXing (Zebra Crossing) dependency to `pom.xml`:
  ```xml
  <dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
  </dependency>
  <dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
  </dependency>
  ```

**What to create:**

**`BarcodeService`** with two methods:
- `byte[] generateBarcode(String content, int width, int height)` — Code 128 linear barcode
- `byte[] generateQrCode(String content, int width, int height)` — QR code

Both return a PNG image as `byte[]`.

**New endpoints (for testing/demo):**
- `GET /api/v1/barcodes/code128?content=MSCU1234567&width=300&height=80` — returns PNG
- `GET /api/v1/barcodes/qr?content=https://example.com/track/FO-001&width=250&height=250` — returns
  PNG

Set `Content-Type: image/png` on the response.

**Hints:**
- Use `MultiFormatWriter` from ZXing for generation
- Use `MatrixToImageWriter` from `zxing-javase` to convert `BitMatrix` → PNG bytes
- Keep the service stateless and injectable — other issues will depend on it

**Acceptance criteria:**
- [ ] ZXing dependencies added
- [ ] `BarcodeService` generates both Code 128 and QR code as PNG
- [ ] Demo endpoints return valid images
- [ ] Unit test verifies generated byte array is a valid PNG (starts with PNG magic bytes)
- [ ] Code is formatted

---

## TRK-002 — Public Tracking Endpoint 🟡

**Labels:** `backend`, `business-logic`, `tracking`
**Depends on:** Phase 1 `CRD-004` (Voyage Controller)

Create a public-facing, read-only endpoint that lets anyone check the status of a freight order or
container by its code. This is the URL that QR codes will point to.

**New endpoints:**
- `GET /api/v1/track/order/{orderId}` — track a freight order
- `GET /api/v1/track/container/{containerCode}` — track a container across all its voyages

**Order tracking response:**
```json
{
  "orderId": 42,
  "status": "IN_TRANSIT",
  "containerCode": "MSCU1234567",
  "containerSize": "TWENTY_FOOT",
  "containerType": "DRY",
  "voyageNumber": "VOY-2025-001",
  "vesselName": "MV Freight Star",
  "departurePort": "Jebel Ali",
  "arrivalPort": "Shanghai",
  "departureTime": "2025-04-01T08:00:00",
  "estimatedArrival": "2025-04-15T18:00:00",
  "voyageStatus": "IN_PROGRESS"
}
```

**Container tracking response:**
```json
{
  "containerCode": "MSCU1234567",
  "size": "TWENTY_FOOT",
  "type": "DRY",
  "voyages": [
    {
      "voyageNumber": "VOY-2025-001",
      "status": "COMPLETED",
      "departurePort": "Jebel Ali",
      "arrivalPort": "Shanghai",
      "departureTime": "2025-03-01T08:00:00",
      "arrivalTime": "2025-03-15T18:00:00"
    },
    {
      "voyageNumber": "VOY-2025-007",
      "status": "IN_PROGRESS",
      "departurePort": "Shanghai",
      "arrivalPort": "Rotterdam",
      "departureTime": "2025-04-01T08:00:00",
      "arrivalTime": "2025-04-20T18:00:00"
    }
  ]
}
```

**What to create:**
- `TrackingController` — separate controller, no auth required
- `TrackingService` — queries orders/containers and assembles the response
- `OrderTrackingResponse` and `ContainerTrackingResponse` DTOs

**Business rules:**
- These endpoints are read-only, no mutations
- Return 404 with a clear message if order or container not found
- Container tracking shows all voyages (via freight orders), sorted by departure time

**Acceptance criteria:**
- [ ] Order tracking returns current status and voyage info
- [ ] Container tracking returns full voyage history
- [ ] 404 for unknown order ID or container code
- [ ] At least 2 tests (one per endpoint)
- [ ] Code is formatted

---

## TRK-003 — Container Label PDF with QR Code 🟠

**Labels:** `backend`, `business-logic`, `tracking`
**Depends on:** `TRK-001`, `TRK-002`

Generate a printable container label PDF that port and warehouse staff can scan.

**New endpoint:**
- `GET /api/v1/containers/{id}/label` — returns PDF (Content-Type: `application/pdf`)

**Label should include:**
- Container code as both human-readable text and Code 128 barcode
- QR code encoding the tracking URL: `{app.base-url}/api/v1/track/container/{containerCode}`
- Container size and type
- If the container is currently booked on an active voyage, include: voyage number, vessel name,
  departure/arrival ports, departure date

**Technical hints:**
- Use the same PDF library from `INV-001` (OpenPDF recommended)
- Inject `BarcodeService` from `TRK-001` to generate barcode/QR images
- Embed the PNG images into the PDF using the library's image API
- Add `app.base-url` to `application.properties` (default: `http://localhost:8080`)

**Label layout suggestion:**
```
┌──────────────────────────────────┐
│  MSCU1234567                     │
│  |||||||||||||||||||||| (Code128)│
│                                  │
│  Size: 20ft    Type: DRY        │
│                                  │
│  Voyage: VOY-2025-001           │
│  Vessel: MV Freight Star        │
│  Jebel Ali → Shanghai           │
│  Departure: 2025-04-01          │
│                                  │
│  ┌─────────┐                    │
│  │  QR     │  Scan to track     │
│  │  Code   │                    │
│  └─────────┘                    │
└──────────────────────────────────┘
```

**Acceptance criteria:**
- [ ] PDF generates with barcode and QR code
- [ ] QR code encodes the correct tracking URL
- [ ] Voyage info shown if container is on an active booking
- [ ] Label works without active booking (shows container info only)
- [ ] Test verifies 200 response with `application/pdf` content type
- [ ] Code is formatted

---

## TRK-004 — Embed QR Code into Invoice PDF 🟡

**Labels:** `backend`, `enhancement`, `tracking`, `invoicing`
**Depends on:** `TRK-001`, `INV-001`

Add a QR code to the invoice PDF that links to the order tracking page.

**Changes to `InvoiceService` (from `INV-001`):**
- After the existing invoice content, add a QR code in the bottom-right area
- QR encodes: `{app.base-url}/api/v1/track/order/{orderId}`
- Add small label text below the QR: "Scan to track your shipment"

**Technical hints:**
- Inject `BarcodeService` from `TRK-001`
- Generate QR as `byte[]`, embed as image in the PDF
- Keep it small (e.g. 100x100 px) so it doesn't dominate the invoice layout

**Acceptance criteria:**
- [ ] Invoice PDF now includes a QR code
- [ ] QR encodes the correct tracking URL
- [ ] Existing invoice tests still pass
- [ ] QR is positioned cleanly and doesn't overlap other content
- [ ] Code is formatted

---

## TRK-005 — Tracking Event Log 🟠

**Labels:** `backend`, `business-logic`, `tracking`
**Depends on:** `TRK-002`

Log status changes and scan events to build a full audit trail for each freight order. This is the
backbone of shipment visibility.

**New entity: `TrackingEvent`**
- `id`, `createdAt`, `updatedAt` (from BaseEntity)
- `freightOrderId` (FK to FreightOrder)
- `eventType` (enum: `STATUS_CHANGE`, `GATE_IN`, `GATE_OUT`, `LOADED`, `DISCHARGED`,
  `CUSTOMS_CLEARED`, `NOTE`)
- `description` (String — e.g. "Status changed from PENDING to CONFIRMED")
- `location` (String, optional — e.g. "Jebel Ali Terminal 2")
- `performedBy` (String, optional — e.g. "scanner-T2-gate" or agent name)
- `eventTime` (LocalDateTime — when it actually happened, may differ from `createdAt`)

**New endpoints:**
- `POST /api/v1/freight-orders/{id}/events` — log a new tracking event
- `GET /api/v1/freight-orders/{id}/events` — list all events for an order (sorted by `eventTime`
  ascending)

**Automatic event creation:**

- When a freight order status changes (in `FreightOrderService`), automatically create a
  `STATUS_CHANGE` event with a description like "Status changed from PENDING to CONFIRMED"
- Manual events (gate scans, notes) are added via the POST endpoint

**Update tracking endpoint (`TRK-002`):**
- Add an `events` array to the order tracking response so the tracking page shows the full timeline

**Event list response:**
```json
[
  {
    "eventType": "STATUS_CHANGE",
    "description": "Order created with status PENDING",
    "location": null,
    "performedBy": "ops-team",
    "eventTime": "2025-03-28T09:15:00"
  },
  {
    "eventType": "GATE_IN",
    "description": "Container entered terminal",
    "location": "Jebel Ali Terminal 2",
    "performedBy": "scanner-T2-gate",
    "eventTime": "2025-03-30T14:22:00"
  },
  {
    "eventType": "LOADED",
    "description": "Container loaded onto MV Freight Star",
    "location": "Jebel Ali Berth 4",
    "performedBy": "crane-operator-B4",
    "eventTime": "2025-04-01T06:45:00"
  },
  {
    "eventType": "STATUS_CHANGE",
    "description": "Status changed from CONFIRMED to IN_TRANSIT",
    "location": null,
    "performedBy": "system",
    "eventTime": "2025-04-01T08:00:00"
  }
]
```

**Acceptance criteria:**
- [ ] Manual events can be logged via POST
- [ ] Status changes automatically create events
- [ ] Events listed in chronological order
- [ ] Tracking endpoint includes event timeline
- [ ] At least 3 tests: manual event, auto status event, event listing
- [ ] Code is formatted

---

## TRK-006 — Gate Pass PDF with QR Code 🔴

**Labels:** `backend`, `business-logic`, `tracking`
**Depends on:** `TRK-001`, `TRK-002`, `TRK-005`, `CST-001`

Generate a gate pass document that authorizes a container to enter or leave a port terminal. This is
a standard document in freight operations.

**New endpoint:**
- `GET /api/v1/freight-orders/{id}/gate-pass` — returns PDF (Content-Type: `application/pdf`)

**Gate pass should include:**
- Gate pass number (auto-generated, e.g. `GP-2025-00042`)
- QR code encoding the tracking URL for the order
- Container code with Code 128 barcode
- Container size and type
- Voyage number, vessel name
- Departure port and arrival port
- Customer company name
- Agent name (if `AGT-001` is done, otherwise `orderedBy`)
- Valid date range (departure date ± 3 days for buffer)
- Large bold text: `GATE IN` or `GATE OUT` (request param: `direction=IN|OUT`)

**New endpoint parameter:**
- `GET /api/v1/freight-orders/{id}/gate-pass?direction=IN`

**Gate pass layout suggestion:**
```
┌─────────────────────────────────────┐
│         *** GATE IN PASS ***        │
│         GP-2025-00042               │
│                                     │
│  Container: MSCU1234567             │
│  |||||||||||||||||||||||| (Code128) │
│  Size: 20ft    Type: DRY           │
│                                     │
│  Voyage: VOY-2025-001              │
│  Vessel: MV Freight Star           │
│  Route: Jebel Ali → Shanghai       │
│                                     │
│  Customer: Acme Trading LLC        │
│  Valid: 2025-03-29 to 2025-04-04   │
│                                     │
│  ┌─────────┐                       │
│  │  QR     │  Scan to track        │
│  │  Code   │                       │
│  └─────────┘                       │
└─────────────────────────────────────┘
```

**Business rules:**
- Gate pass can only be generated for orders with status `PENDING`, `CONFIRMED`, or `IN_TRANSIT`
- Return 409 if order is `CANCELLED` or `DELIVERED`
- `direction` parameter is required, return 400 if missing

**Acceptance criteria:**
- [ ] PDF generates with both barcode and QR code
- [ ] Direction (IN/OUT) shown prominently
- [ ] Valid date range calculated from voyage departure
- [ ] Returns 409 for cancelled/delivered orders
- [ ] Returns 400 for missing direction parameter
- [ ] Test verifies PDF generation for both directions
- [ ] Code is formatted

---

## AIP-001 — AI Service Abstraction and LLM Integration 🟡

**Labels:** `backend`, `ai`, `infrastructure`

Create a provider-agnostic AI service layer that can talk to any LLM (Claude, OpenAI, Ollama, etc.)
without leaking provider details into business logic.

**What to create:**

**Interface: `AiClient`**
```java
public interface AiClient {
  String complete(String systemPrompt, String userPrompt);
}
```

**Implementations (pick one as default, wire via Spring profile):**
- `ClaudeAiClient` — calls Anthropic Messages API
- `OpenAiClient` — calls OpenAI Chat Completions API

Use `RestClient` (Spring 6.1+) for HTTP calls. No SDK dependencies — keep it lean.

**Configuration:**
```properties
# application.properties
app.ai.provider=claude            # or "openai" or "ollama"
app.ai.api-key=${AI_API_KEY}
app.ai.model=claude-sonnet-4-20250514
app.ai.max-tokens=1024
app.ai.base-url=https://api.anthropic.com
```

**Spring wiring:**

- Use `@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude")` to auto-select the
  implementation
- Create an `AiConfig` class that reads properties and builds the active client bean

**New endpoint (for smoke testing):**
- `POST /api/v1/ai/test` — accepts `{ "prompt": "..." }`, returns raw LLM response
- This endpoint should be disabled in production via a property flag

**Hints:**
- Don't add SDKs — just raw HTTP with `RestClient`
- Always set a timeout (30s suggested)
- Log token usage if the provider returns it
- Add a `NoOpAiClient` implementation that returns a canned response — useful for tests

**Acceptance criteria:**
- [ ] `AiClient` interface defined
- [ ] At least one real provider implemented (Claude or OpenAI)
- [ ] `NoOpAiClient` available for tests
- [ ] Provider selected via `app.ai.provider` property
- [ ] API key read from environment variable, not hardcoded
- [ ] Smoke test endpoint works
- [ ] Test using `NoOpAiClient` verifies wiring without real API calls
- [ ] Code is formatted

---

## AIP-002 — AI Price Suggestion from Historical Data 🟠

**Labels:** `backend`, `ai`, `business-logic`, `pricing`
**Depends on:** `AIP-001`, `PRC-001`

When setting a voyage price, the system should suggest a price range based on historical pricing
data from past voyages on the same or similar routes.

**New endpoint:**
- `GET /api/v1/voyages/{voyageId}/price-suggestion?containerSize=TWENTY_FOOT`

**How it works:**

1. Query historical `VoyagePrice` + `Voyage` data for the same route (same departure/arrival port
   pair) or similar routes (same region)
2. Gather: past prices, container sizes, voyage dates, number of orders per voyage
3. Build a structured prompt with this data and ask the LLM to suggest a price range with reasoning
4. Parse the LLM response into a structured DTO

**What to create:**

- `PriceSuggestionService` — gathers historical data, builds prompt, calls `AiClient`, parses
  response
- `PriceSuggestionResponse` DTO

**Response:**
```json
{
  "voyageNumber": "VOY-2025-010",
  "route": "Jebel Ali → Shanghai",
  "containerSize": "TWENTY_FOOT",
  "suggestedPriceLowUsd": 1100.00,
  "suggestedPriceHighUsd": 1350.00,
  "confidence": "MEDIUM",
  "reasoning": "Based on 12 past voyages on this route over the last 6 months, the average price was $1,180/TEU. Recent voyages show an upward trend of ~8%. Suggested range accounts for seasonal demand increase in Q2.",
  "dataPoints": 12,
  "historicalAvgUsd": 1180.00,
  "historicalMinUsd": 950.00,
  "historicalMaxUsd": 1400.00
}
```

**Prompt engineering hints:**
- Include structured data in the prompt (route, dates, prices as a table)
- Ask for JSON output with specific fields
- Include instructions: "If fewer than 3 data points, set confidence to LOW and note insufficient
  data"
- Ask the LLM to explain its reasoning in 2–3 sentences

**Confidence levels:**
- `HIGH` — 10+ data points, consistent pricing, same route
- `MEDIUM` — 3–9 data points or similar (not identical) routes used
- `LOW` — fewer than 3 data points or no matching routes

**Edge cases:**

- No historical data for this route → return confidence `LOW` with a message suggesting manual
  pricing
- Route has data but only for the other container size → note this in reasoning

**Acceptance criteria:**
- [ ] Endpoint returns a structured price suggestion
- [ ] Historical data gathered correctly from past voyages
- [ ] Confidence level reflects data quality
- [ ] Works gracefully with zero historical data
- [ ] Test with `NoOpAiClient` verifies the flow end-to-end
- [ ] Code is formatted

---

## AIP-003 — Market Data Integration for Enriched Suggestions 🟠

**Labels:** `backend`, `ai`, `business-logic`, `pricing`
**Depends on:** `AIP-002`

Enrich the price suggestion by pulling in external market rate data so the LLM can compare internal
pricing against the broader market.

**What to create:**

**Interface: `MarketDataProvider`**
```java
public interface MarketDataProvider {
  Optional<MarketRate> getCurrentRate(String originPort, String destPort, ContainerSize size);
}
```

**`MarketRate` DTO:**
```java
public class MarketRate {
  private BigDecimal spotRateUsd;
  private String source;          // e.g. "Freightos Baltic Index"
  private LocalDate asOfDate;
  private String route;           // e.g. "China → North Europe"
}
```

**Implementations:**
- `FreightosMarketDataProvider` — calls Freightos FBX API (free tier available)
- `StaticMarketDataProvider` — returns hardcoded sample rates for demo/testing

**Configuration:**
```properties
app.market-data.provider=static   # or "freightos"
app.market-data.api-key=${MARKET_DATA_API_KEY}
```

**Update `PriceSuggestionService` (from `AIP-002`):**
- Before calling the LLM, fetch current market rate
- Include it in the prompt: "The current market spot rate for this route is $X/TEU as of [date]"
- Update the response DTO:

```json
{
  "suggestedPriceLowUsd": 1200.00,
  "suggestedPriceHighUsd": 1450.00,
  "confidence": "HIGH",
  "reasoning": "Historical average is $1,180/TEU across 12 voyages. Current market spot rate is $1,380/TEU (Freightos Baltic Index, 2025-03-28), indicating upward pressure. Suggested range positions you competitively below spot while maintaining margin.",
  "marketRate": {
    "spotRateUsd": 1380.00,
    "source": "Freightos Baltic Index",
    "asOfDate": "2025-03-28"
  },
  "dataPoints": 12,
  "historicalAvgUsd": 1180.00
}
```

**Acceptance criteria:**
- [ ] Market data provider abstraction defined
- [ ] At least `StaticMarketDataProvider` implemented
- [ ] Market rate included in LLM prompt and response
- [ ] Suggestion works gracefully when market data is unavailable
- [ ] Test with static provider verifies enriched suggestion
- [ ] Code is formatted

---

## AIP-004 — Risk Factor Analysis from News and Events 🔴

**Labels:** `backend`, `ai`, `business-logic`, `pricing`
**Depends on:** `AIP-002`

Add qualitative risk factor analysis by feeding recent shipping-related news into the price
suggestion. The LLM synthesizes news headlines into risk factors that may affect pricing.

**What to create:**

**Interface: `NewsProvider`**
```java
public interface NewsProvider {
  List<NewsItem> getRecentHeadlines(String route, int maxResults);
}
```

**`NewsItem` DTO:**
```java
public class NewsItem {
  private String headline;
  private String source;
  private LocalDate publishedDate;
  private String summary;   // 1-2 sentences
}
```

**Implementations:**
- `RssNewsProvider` — fetches from public RSS feeds (e.g. Lloyd's List, The Loadstar, gCaptain)
- `StaticNewsProvider` — returns sample headlines for demo/testing

**Configuration:**
```properties
app.news.provider=static   # or "rss"
app.news.feeds=https://gcaptain.com/feed/,https://theloadstar.com/feed/
```

**Update `PriceSuggestionService`:**
- Fetch recent headlines relevant to the route (keyword matching on port names, regions)
- Include top 5 headlines in the LLM prompt
- Ask the LLM to identify risk factors and their potential impact on pricing
- Add to response:

```json
{
  "suggestedPriceLowUsd": 1300.00,
  "suggestedPriceHighUsd": 1550.00,
  "confidence": "MEDIUM",
  "reasoning": "...",
  "riskFactors": [
    {
      "factor": "Red Sea routing disruption",
      "impact": "HIGH",
      "description": "Ongoing Houthi attacks forcing vessels via Cape of Good Hope, adding 10-14 days transit time and increasing fuel costs."
    },
    {
      "factor": "Shanghai port congestion",
      "impact": "MEDIUM",
      "description": "Reports of 3-5 day delays at Shanghai terminals due to surge in export volumes ahead of Q2."
    }
  ]
}
```

**Important:** The LLM is synthesizing and reasoning about news — not making predictions. Frame it
as "risk factors to consider" not "price will go up 15%."

**Acceptance criteria:**
- [ ] News provider abstraction defined
- [ ] At least `StaticNewsProvider` implemented
- [ ] Risk factors included in suggestion response
- [ ] LLM prompt clearly separates facts from analysis
- [ ] Works gracefully when no relevant news found
- [ ] Test with static provider verifies risk factor extraction
- [ ] Code is formatted

---

## AIP-005 — Unified Price Intelligence Endpoint 🟠

**Labels:** `backend`, `ai`, `business-logic`, `pricing`
**Depends on:** `AIP-002` (required), `AIP-003` (optional), `AIP-004` (optional)

Combine all available intelligence into a single endpoint that assembles whatever data sources are
available and produces the richest possible suggestion.

**Update endpoint:**
- `GET /api/v1/voyages/{voyageId}/price-intelligence?containerSize=TWENTY_FOOT`

This replaces / wraps the endpoint from `AIP-002`. It gracefully degrades:
- If only historical data is available → Tier 1 suggestion
- If market data is configured → Tier 2 enriched suggestion
- If news feeds are configured → Tier 3 with risk factors
- Response always indicates which data sources were used

**Full response:**
```json
{
  "voyageNumber": "VOY-2025-010",
  "route": "Jebel Ali → Shanghai",
  "containerSize": "TWENTY_FOOT",
  "suggestion": {
    "lowUsd": 1300.00,
    "highUsd": 1550.00,
    "confidence": "HIGH",
    "reasoning": "..."
  },
  "historicalData": {
    "dataPoints": 12,
    "avgUsd": 1180.00,
    "minUsd": 950.00,
    "maxUsd": 1400.00
  },
  "marketData": {
    "spotRateUsd": 1380.00,
    "source": "Freightos Baltic Index",
    "asOfDate": "2025-03-28"
  },
  "riskFactors": [
    { "factor": "...", "impact": "HIGH", "description": "..." }
  ],
  "dataSources": ["historical", "market", "news"],
  "generatedAt": "2025-03-28T14:30:00"
}
```

**What to create:**

- `PriceIntelligenceService` — orchestrator that gathers all available data, builds combined prompt,
  calls `AiClient`
- `PriceIntelligenceResponse` DTO
- Refactor `PriceSuggestionService` from `AIP-002` into modular data gatherers that
  `PriceIntelligenceService` calls

**Business rules:**

- `dataSources` array reflects what was actually used (not what's configured — if market API is
  down, omit it)
- Each data source failure should be logged but not break the endpoint
- If `AiClient` itself fails, return 503 with a message

**Acceptance criteria:**
- [ ] Endpoint assembles all available data sources
- [ ] Gracefully degrades when sources are unavailable
- [ ] `dataSources` accurately reflects what was used
- [ ] Returns 503 if AI service is unreachable
- [ ] Test with `NoOpAiClient` + static providers verifies full flow
- [ ] Test with only historical data verifies graceful degradation
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
  ├──→ INV-002 (Email Invoice)
  └──→ TRK-006 (Gate Pass PDF)

INV-001 (Invoice PDF)
  ├──→ INV-002 (Email Invoice)
  │    ├──→ AGT-003 (Email Commission)
  │    └──→ NTF-001 (Notification Engine)
  └──→ TRK-004 (QR on Invoice)

NTF-001 (Notification Engine)
  ├──→ NTF-002 (Booking Confirm/Cancel)
  ├──→ NTF-003 (Departure Notice)
  ├──→ NTF-004 (Arrival Notice)
  ├──→ NTF-005 (Delivery Confirmation)
  └──→ NTF-006 (Voyage Delay Alert)

VPL-001 (Load Tracking)
  └──→ VPL-002 (Auto Cutoff)

FIN-001 (Vessel Ownership)
  └──→ FIN-002 (Financial Summary)

AGT-001 (Agent Entity)
  └──→ AGT-002 (Commission Calc)
       └──→ AGT-003 (Email Commission)

TRK-001 (Barcode Service)
  ├──→ TRK-003 (Container Label PDF)
  ├──→ TRK-004 (QR on Invoice)
  └──→ TRK-006 (Gate Pass PDF)

TRK-002 (Public Tracking)
  ├──→ TRK-003 (Container Label PDF)
  ├──→ TRK-005 (Tracking Events)
  │    └──→ TRK-006 (Gate Pass PDF)
  └──→ TRK-004 (QR on Invoice)

AIP-001 (AI Service)
  └──→ AIP-002 (Historical Suggestion)
       ├──→ AIP-003 (Market Data)
       ├──→ AIP-004 (News Risk Factors)
       └──→ AIP-005 (Unified Intelligence)
            ├── uses AIP-003 (optional)
            └── uses AIP-004 (optional)
```

## Suggested Team Allocation

| Track | Issues                                        | Can start when                               |
|-------|-----------------------------------------------|----------------------------------------------|
| A     | `PRC-001` → `PRC-002` → `INV-001` → `INV-002` | Phase 1 complete                             |
| B     | `VPL-001` → `VPL-002`                         | Phase 1 `CRD-004` complete                   |
| C     | `FIN-001` → `FIN-002`                         | `PRC-001` + `FIN-001` complete               |
| D     | `AGT-001` → `AGT-002` → `AGT-003`             | `PRC-001` + `AGT-001` complete               |
| E     | `TRK-001` → `TRK-002` → `TRK-005` → `TRK-006` | `TRK-001` anytime; `TRK-006` needs `CST-001` |
| F     | `AIP-001` → `AIP-002` → `AIP-005`             | `AIP-001` anytime; `AIP-002` needs `PRC-001` |
| G     | `NTF-001` → `NTF-002` → `NTF-003` → `NTF-004` | `NTF-001` needs `INV-002` + `CST-001`        |
| —     | `CST-001`                                     | Anytime                                      |
| —     | `TRK-003`, `TRK-004`                          | After their dependencies                     |
| —     | `AIP-003`, `AIP-004`                          | After `AIP-002`, independent of each other   |
| —     | `NTF-005`                                     | After `NTF-001`, pairs well with `INV-001`   |
| —     | `NTF-006`                                     | After `NTF-001` + `CRD-004`                  |

Tracks A, B, E, and F can all run in parallel. Track G starts once Track A delivers `INV-002`.
`NTF-005` and `NTF-006` are standalone pickups once `NTF-001` is done.