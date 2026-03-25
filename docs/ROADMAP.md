# Roadmap

Version plan for Freight Operations API. Each version represents a shippable milestone — earlier
versions are well-defined, later ones are directional.

## Version Summary

| Version | Codename          | Theme                                   | Status         |
|---------|-------------------|-----------------------------------------|----------------|
| 0.1.0   | Foundation        | Boilerplate, models, one working sample | ✅ Done         |
| 0.2.0   | CRUD Complete     | All entity controllers + dev experience | ✅ Done         |
| 0.3.0   | Money Talks       | Pricing, discounts, customers           | ✅ Done         |
| 0.4.0   | Paper Trail       | Invoicing, PDF generation, QR tracking  | ✅ Done         |
| 0.5.0   | Ship Shape        | Vessel planning, load control, cutoffs  | ✅ Done         |
| 0.6.0   | Track & Trace     | Barcodes, QR codes, tracking events     | ✅ Done         |
| 0.7.0   | Show Me the Money | Ownership, financials, commissions      | ✅ Done         |
| 0.8.0   | Smart Pricing     | AI-powered price intelligence           | ✅ Done         |
| 0.9.0   | Solid Ground      | Infrastructure hardening + data cleanup | 🔧 In progress |
| 0.9.5   | Bulk Carrier      | Bulk cargo mode (DWT-based)             | 📋 Planned     |
| 1.0.0   | Production Ready  | Auth, audit, deployment, hardening      | 🔮 Vision      |

---

## 0.1.0 — Foundation ✅

The boilerplate that started it all.

**Delivered:**

- Spring Boot 3.4.x project with JPA, PostgreSQL, Docker Compose
- Domain entities: Port, Vessel, Container, Voyage, FreightOrder
- One fully working controller (`FreightOrderController`) as a reference
- Sample JUnit 5 integration test
- Google Java Format enforcement
- README, CONTRIBUTING, and issue backlog

---

## 0.2.0 — CRUD Complete ✅

All entities get their own controllers. Developer experience improvements.

**Delivered:**

- `CRD-001` — Port CRUD controller
- `CRD-002` — Vessel CRUD controller
- `CRD-003` — Container CRUD controller
- `CRD-004` — Voyage controller with status transitions
- `ENH-001` — Pagination on list endpoints (`PageResponse<T>`)
- `ENH-002` — Swagger / OpenAPI documentation
- `ENH-003` — List containers on a voyage
- `ENH-004` — Prevent double-booking a container
- `ENH-005` — Lombok introduced across entities and DTOs

---

## 0.3.0 — Money Talks ✅

Freight orders get real prices. Customers enter the picture.

**Delivered:**

- `PRC-001` — Voyage pricing model (`VoyagePrice` per container size)
- `PRC-002` — Discount support on freight orders
- `CST-001` — Customer entity linked to freight orders

---

## 0.4.0 — Paper Trail ✅

Generate professional documents and embed tracking QR codes.

**Delivered:**

- `INV-001` — Invoice PDF generation (iText 5)
- `INV-002` — Email invoice to customer
- QR code embedded in invoice PDF linking to the public tracking endpoint

---

## 0.5.0 — Ship Shape ✅

Operational control over vessel capacity and bookings.

**Delivered:**

- `VPL-001` — Voyage load tracking and manual booking stop
- `VPL-002` — Automatic booking cutoff based on TEU utilisation (`BookingProperties`)

---

## 0.6.0 — Track & Trace ✅

Real-world tracking with barcodes and event logs.

**Delivered:**

- `TRK-001` — Barcode and QR code generation service (ZXing)
- `TRK-002` — Public tracking endpoint (`/api/v1/track/order/{orderId}`)
- `TRK-003` — Container label PDF with QR code
- `TRK-004` — QR code embedded in invoice PDF
- `TRK-005` — Tracking event log

---

## 0.7.0 — Show Me the Money ✅

Financial transparency for vessel owners and agents.

**Delivered:**

- `FIN-001` — Vessel ownership model (multi-owner with share percentages)
- `FIN-002` — Voyage financial summary with owner profit split
- `AGT-001` — Agent / freight forwarder entity and commission tracking

---

## 0.8.0 — Smart Pricing ✅

AI-powered pricing intelligence.

**Delivered:**

- `AIP-001` — Provider-agnostic AI service abstraction (Claude, OpenAI, NoOp)
- LLM-powered price suggestion endpoint

---

## 0.9.0 — Solid Ground 🔧

Infrastructure hardening and data model cleanup. These deliver the foundation that 1.0.0 depends on.

**Issues:**

- `INF-001` — Replace `ddl-auto=update` with Flyway versioned migrations
- `ENH-006` — Expose `id` on `VesselResponse` and `VoyageResponse`
- `ENH-007` — Negative-path test coverage (404, 400, business rule violations)
- `ENH-008` — Replace `orderedBy` free-text with structured `Operator` entity

**Release criteria:**

- [ ] Flyway baseline migration covers all current tables
- [ ] All future schema changes delivered as numbered migration files
- [ ] `VesselResponse` and `VoyageResponse` include `id`
- [ ] Every controller has at least one 404 and one 400 negative test
- [ ] `FreightOrder` references `Operator` by FK, not free-text string
- [ ] All tests green; format check passes

---

## 0.9.5 — Bulk Carrier 📋

Extend the freight model to support bulk cargo (tonnes-based) alongside containers.

**Issues:**

- `CRG-001` — Bulk cargo support (DWT capacity, `BulkCargo` entity, `VoyageBulkRate`)

**Release criteria:**

- [ ] `BulkCargo` entity and endpoints
- [ ] `VoyageBulkRate` pricing and management endpoints
- [ ] `FreightOrder` supports `cargoMode = BULK`
- [ ] `Vessel` carries optional DWT; bulk voyages validate against it
- [ ] `/voyages/{id}/bulk-load` DWT utilisation endpoint
- [ ] Integration tests: create bulk order, DWT cutoff, reject bulk on container voyage
- [ ] Container cargo tests unaffected

---

## 1.0.0 — Production Ready 🔮

_Not yet broken into issues. This is the vision for what a production release would need._

### Authentication & Authorization

- JWT-based auth or OAuth2/OIDC integration
- Role-based access: ADMIN, OPS_MANAGER, OPS_AGENT, CUSTOMER (read-only)
- API key support for external freight forwarders
- Rate limiting per client
- `Operator` entity (from ENH-008) linked to auth identity

### Audit & Compliance

- Full audit log on all mutations (who changed what, when)
- Soft deletes instead of hard deletes
- Data retention policies
- GDPR-aware customer data handling

### Operational Hardening

- Connection pooling tuned for production (HikariCP settings)
- Health checks and readiness probes (`/actuator/health`)
- Structured JSON logging (Logback + ELK or similar)
- Correlation IDs across requests

### Deployment

- Dockerfile for the application (multi-stage build)
- Docker Compose with app + DB + MailHog for full local stack
- Environment-specific configs (dev, staging, production)
- Kubernetes manifests or Helm chart (stretch)

### Monitoring & Observability

- Micrometer metrics (order throughput, response times, AI call latency)
- Prometheus + Grafana dashboards
- Distributed tracing (OpenTelemetry)
- Alerting on error rates and AI service failures

### Performance

- Database indexes on frequently queried columns
- Caching layer (Redis) for voyage prices and market data
- Async processing for email sending and PDF generation
- Bulk operations for high-volume order creation

### Future Product Ideas

- Customer portal — external customers track their own shipments
- Mobile scanning app — port workers scan QR codes to log events
- Route optimization — AI suggests optimal vessel routing
- Demand forecasting — predict booking volumes per route/season
- Multi-currency support — price in local currencies with exchange rates
- Bill of Lading generation — the core shipping document
- AI price suggestion for bulk cargo (follow-up to CRG-001)
- Integration with terminal operating systems (TOS) for live container status

---

## Version Branching Strategy

| Branch            | Purpose                              |
|-------------------|--------------------------------------|
| `master`          | Latest stable release                |
| `develop`         | Integration branch for next version  |
| `feature/XXX-NNN` | Individual issue work                |
| `release/0.X.0`   | Release stabilization before tagging |

**Flow:** `feature/XXX-NNN` → PR to `develop` → when version is complete, `develop` → PR to
`release/0.X.0` → tag → merge to `master`

---

_Last updated: March 2026_
