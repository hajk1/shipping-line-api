# Freight Operations Platform — System Overview

**Purpose:** Internal platform for managing container shipping operations — from scheduling voyages
to booking freight, tracking shipments, and generating invoices.

---

## 1. Core Concepts

| Concept           | What it is                                                                                |
|-------------------|-------------------------------------------------------------------------------------------|
| **Port**          | A physical port location identified by a 5-character code (e.g. AEJEA = Jebel Ali, Dubai) |
| **Vessel**        | A cargo ship with a fixed TEU capacity (e.g. 3,000 TEU)                                   |
| **Container**     | A physical shipping container — 20ft (1 TEU) or 40ft (2 TEU), various cargo types         |
| **Voyage**        | A scheduled trip of one vessel between two ports on specific dates                        |
| **Freight Order** | A booking: one container on one voyage, for one customer, priced and tracked              |
| **Invoice**       | A PDF billing document generated once an order reaches DELIVERED status                   |
| **Agent**         | An internal or external sales representative who places orders and earns commission       |
| **Customer**      | The shipping client whose cargo is being transported                                      |
| **Operator**      | An internal system user who creates freight orders (replaces free-text `orderedBy`)       |

---

## 2. Data Model

```mermaid
erDiagram
    VESSEL ||--o{ VOYAGE : "operates"
    VESSEL ||--o{ VESSEL_OWNER : "owned by"
    PORT ||--o{ VOYAGE : "departure"
    PORT ||--o{ VOYAGE : "arrival"
    VOYAGE ||--o{ FREIGHT_ORDER : "carries"
    VOYAGE ||--o{ VOYAGE_PRICE : "priced by size"
    VOYAGE ||--o{ VOYAGE_COST : "incurs costs"
    CONTAINER ||--o{ FREIGHT_ORDER : "assigned to"
    CUSTOMER ||--o{ FREIGHT_ORDER : "places"
    AGENT ||--o{ FREIGHT_ORDER : "manages"
    FREIGHT_ORDER ||--o{ TRACKING_EVENT : "has history"
    FREIGHT_ORDER ||--o| INVOICE : "billed via"

    VESSEL {
        string name
        string imoNumber
        int capacityTeu
    }
    PORT {
        string unlocode
        string name
        string country
    }
    VOYAGE {
        string voyageNumber
        datetime departureTime
        datetime arrivalTime
        enum status
        bool bookingOpen
    }
    CONTAINER {
        string containerCode
        enum size
        enum type
    }
    FREIGHT_ORDER {
        enum status
        decimal basePriceUsd
        decimal discountPercent
        decimal finalPrice
    }
    TRACKING_EVENT {
        enum eventType
        string description
        string location
        datetime eventTime
    }
```

---

## 3. Freight Order Lifecycle

A freight order moves through five states from creation to billing.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> PENDING : Order created
    PENDING --> CONFIRMED : Booking confirmed
    CONFIRMED --> IN_TRANSIT : Vessel departs
    IN_TRANSIT --> DELIVERED : Vessel arrives
    DELIVERED --> [*] : Invoice generated

    PENDING --> CANCELLED : Cancelled before confirmation
    CONFIRMED --> CANCELLED : Cancelled after confirmation
```

---

## 4. Main Business Flows

### 4.1 Booking a Shipment

```mermaid
sequenceDiagram
    actor Ops as Operations Team
    participant API
    participant System

    Ops->>API: Create Voyage (vessel + ports + dates)
    API-->>Ops: Voyage ID

    Ops->>API: Set price per container size
    API-->>Ops: Price confirmed

    Ops->>API: Place Freight Order (container + voyage + customer)
    API->>System: Check: is voyage open? is capacity available? is container free?
    alt All checks pass
        System-->>API: Order created (PENDING)
        API-->>Ops: Order ID
    else Capacity full (≥95%)
        System-->>API: Booking automatically closed
        API-->>Ops: 400 — Booking closed
    else Container already booked
        API-->>Ops: 400 — Double booking rejected
    end
```

### 4.2 Shipment Tracking (Public)

```mermaid
sequenceDiagram
    actor Customer
    participant TrackingAPI as Public Tracking API

    Customer->>TrackingAPI: GET /track/order/{orderId}
    TrackingAPI-->>Customer: Status, voyage details, full event history

    Customer->>TrackingAPI: GET /track/container/{code}
    TrackingAPI-->>Customer: Container info + all voyages it has been on
```

### 4.3 Invoice Generation

```mermaid
flowchart LR
    A[Order status = DELIVERED] --> B[Request invoice\nGET /freight-orders/id/invoice]
    B --> C[System generates PDF]
    C --> D[PDF includes:\n• Customer & billing info\n• Voyage & container details\n• Pricing breakdown\n• QR code → live tracking]
    D --> E[Invoice record saved]
    E --> F[PDF returned to caller]
```

### 4.4 AI-Assisted Pricing

```mermaid
flowchart TD
    A[Operations team requests\nprice suggestion] --> B{Historical data\non same route?}
    B -- Yes --> C[Fetch last 50 voyages\non same route]
    B -- No --> D{Similar region\ndata available?}
    D -- Yes --> E[Fetch nearby route data]
    D -- No --> F[Return LOW confidence\nfallback — no data]
    C --> G[Send data to AI model\nClaude / OpenAI]
    E --> G
    G --> H{AI response\nvalid?}
    H -- Yes --> I[Return price range\n+ confidence level\n+ reasoning]
    H -- No --> J[Return LOW confidence\nfallback — AI error]
```

---

## 5. Voyage Financial Summary

Available once a voyage is **COMPLETED**. Aggregates all financial data in one view.

```mermaid
flowchart LR
    A[Voyage COMPLETED] --> B[Financial Summary]
    B --> C[Revenue\nSum of all final order prices]
    B --> D[Costs\nFuel · Port fees · Other]
    B --> E[Profit\nRevenue minus Costs]
    B --> F[Load factor\nTEU booked vs capacity]
    B --> G[Agent commissions\nper agent breakdown]
```

---

## 6. Container Label

Every container can produce a printable PDF label on demand.

```mermaid
flowchart LR
    A[GET /containers/id/label] --> B[System builds PDF]
    B --> C[Container code\n+ barcode]
    B --> D[Size & type]
    B --> E[Active voyage info\nroute · vessel · departure date]
    B --> F[QR code\n→ public tracking URL]
```

---

## 7. API Surface Summary

| Domain          | Endpoints     | Key Actions                                            |
|-----------------|---------------|--------------------------------------------------------|
| Ports           | 3             | Create, get, list                                      |
| Vessels         | 3 + ownership | Create, list, manage fractional ownership              |
| Voyages         | 10            | Full lifecycle management, pricing, costing, analytics |
| Freight Orders  | 7             | Book, price, track, invoice                            |
| Customers       | 3             | Create, list, get                                      |
| Agents          | 4             | Create, list, update commission & status               |
| Public Tracking | 2             | Track by order ID or container code — no auth required |
| AI Pricing      | 1             | Price range suggestion with confidence level           |
| Container Label | 1             | On-demand PDF label with QR code                       |

---

## 8. Key Business Rules

- **No double booking** — a container can only appear on one open voyage at a time.
- **Automatic cutoff** — when a voyage reaches 95% TEU capacity, bookings close automatically.
- **Invoice gate** — invoices can only be generated for orders in DELIVERED status.
- **Discount tracking** — every discount requires a stated reason and is preserved on the invoice.
- **Agent commission** — commission percentage is recorded per order for financial reporting.
- **One cargo mode per voyage** — a voyage carries either container cargo (TEU) or bulk cargo
  (tonnes), never both.

---

## 9. Planned: Bulk Cargo Support

The platform currently handles containerised cargo only. A planned extension (`CRG-001`) will add
support for **bulk cargo** — grain, coal, iron ore, crude oil — which is transported differently:

| Dimension       | Container Cargo     | Bulk Cargo (planned)             |
|-----------------|---------------------|----------------------------------|
| Unit            | TEU (20 ft / 40 ft) | Metric tonnes or cubic metres    |
| Vessel capacity | TEU capacity        | Deadweight tonnage (DWT)         |
| Booking unit    | One container       | Quantity in tonnes per commodity |
| Price basis     | Per container size  | Per tonne per commodity type     |

When implemented, freight orders will carry a `cargoMode` flag (`CONTAINER` or `BULK`). Bulk
voyages will validate total booked tonnes against the vessel's DWT, and a new
`/voyages/{id}/bulk-load` endpoint will provide DWT utilisation visibility — the equivalent of the
existing TEU load endpoint.
