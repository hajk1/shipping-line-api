# Roadmap

Version plan for Freight Operations API. Each version represents a shippable milestone — earlier
versions are well-defined, later ones are directional.

## Version Summary

| Version | Codename          | Theme                                   | Status         |
|---------|-------------------|-----------------------------------------|----------------|
| 0.1.0   | Foundation        | Boilerplate, models, one working sample | ✅ Done         |
| 0.2.0   | CRUD Complete     | All entity controllers + dev experience | 🔧 In progress |
| 0.3.0   | Money Talks       | Pricing, discounts, customers           | 📋 Planned     |
| 0.4.0   | Paper Trail       | Invoicing, email, PDF generation        | 📋 Planned     |
| 0.4.5   | Keep Me Posted    | Customer notifications & alerts         | 📋 Planned     |
| 0.5.0   | Ship Shape        | Vessel planning, load control, cutoffs  | 📋 Planned     |
| 0.6.0   | Track & Trace     | Barcodes, QR codes, tracking events     | 📋 Planned     |
| 0.7.0   | Show Me the Money | Ownership, financials, commissions      | 📋 Planned     |
| 0.8.0   | Smart Pricing     | AI-powered price intelligence           | 📋 Planned     |
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

## 0.2.0 — CRUD Complete

All entities get their own controllers. Developer experience improvements.

**Issues:**
- `CRD-001` — Port CRUD controller
- `CRD-002` — Vessel CRUD controller
- `CRD-003` — Container CRUD controller
- `CRD-004` — Voyage controller with status transitions
- `ENH-001` — Pagination on list endpoints
- `ENH-002` — Swagger / OpenAPI documentation
- `ENH-003` — List containers on a voyage
- `ENH-004` — Prevent double-booking a container
- `ENH-005` — Introduce Lombok

**Release criteria:**
- [ ] All 5 entities have full CRUD
- [ ] Swagger UI accessible
- [ ] Pagination on all list endpoints
- [ ] Double-booking prevention in place
- [ ] All tests green, code formatted

---

## 0.3.0 — Money Talks

Freight orders get real prices. Customers enter the picture.

**Issues:**
- `PRC-001` — Voyage pricing model
- `PRC-002` — Discount support on freight orders
- `CST-001` — Customer entity

**Release criteria:**
- [ ] Voyages have per-size pricing
- [ ] Orders automatically priced from voyage
- [ ] Discounts applicable at creation and later
- [ ] Customers linked to freight orders

---

## 0.4.0 — Paper Trail

Generate and deliver professional documents.

**Issues:**
- `INV-001` — Invoice PDF generation
- `INV-002` — Email invoice to customer

**Release criteria:**
- [ ] PDF invoices downloadable for delivered orders
- [ ] Email delivery with PDF attachment
- [ ] SMTP configurable for local dev (MailHog)

---

## 0.4.5 — Keep Me Posted

Customers stay informed throughout the shipment lifecycle. Builds the notification engine and all
lifecycle emails.

**Issues:**
- `NTF-001` — Notification template engine
- `NTF-002` — Booking confirmation and cancellation notices
- `NTF-003` — Departure notice
- `NTF-004` — Arrival notice (advance, scheduled)
- `NTF-005` — Delivery confirmation notice
- `NTF-006` — Voyage delay alert

**Release criteria:**
- [ ] Reusable notification engine with DB-stored templates
- [ ] Automatic emails on: booking confirmed, cancelled, departed, arriving soon, delivered
- [ ] Delay alerts when voyage arrival time is pushed
- [ ] Advance arrival notice via daily scheduler (configurable days-before)
- [ ] Consolidated emails for multi-container customers
- [ ] All notifications logged with sent/failed status
- [ ] Email failures never block business operations

---

## 0.5.0 — Ship Shape

Operational control over vessel capacity and bookings.

**Issues:**
- `VPL-001` — Voyage load tracking and manual booking stop
- `VPL-002` — Automatic booking cutoff based on capacity

**Release criteria:**
- [ ] TEU utilization visible per voyage
- [ ] Manual booking open/close
- [ ] Auto-cutoff at configurable threshold
- [ ] Orders rejected when capacity exceeded

---

## 0.6.0 — Track & Trace

Real-world tracking with barcodes and event logs.

**Issues:**
- `TRK-001` — Barcode and QR code generation service
- `TRK-002` — Public tracking endpoint
- `TRK-003` — Container label PDF with QR code
- `TRK-004` — Embed QR code into invoice PDF
- `TRK-005` — Tracking event log
- `TRK-006` — Gate pass PDF

**Release criteria:**
- [ ] Containers and orders trackable via public URL
- [ ] QR codes on invoices, container labels, and gate passes
- [ ] Event timeline shows full shipment history
- [ ] Scan events loggable via API

---

## 0.7.0 — Show Me the Money

Financial transparency for vessel owners and agents.

**Issues:**
- `FIN-001` — Vessel ownership model
- `FIN-002` — Voyage financial summary with owner profit split
- `AGT-001` — Agent / freight forwarder entity
- `AGT-002` — Commission calculation per agent
- `AGT-003` — Email commission report to agents

**Release criteria:**
- [ ] Multi-owner vessels with share percentages
- [ ] Post-voyage financial breakdown per owner
- [ ] Agents replace free-text `orderedBy`
- [ ] Commission calculated and emailable per agent

---

## 0.8.0 — Smart Pricing

AI-powered pricing intelligence — the differentiator.

**Issues:**
- `AIP-001` — AI service abstraction + LLM integration
- `AIP-002` — Price suggestion from historical data
- `AIP-003` — Market data integration (FBX/external rates)
- `AIP-004` — Risk factor analysis from news/events
- `AIP-005` — Unified price intelligence endpoint

**Release criteria:**
- [ ] Provider-agnostic AI service (Claude, OpenAI, Ollama)
- [ ] Price suggestions based on internal historical data
- [ ] Market rate enrichment from external sources
- [ ] News-based risk factor analysis
- [ ] Single endpoint with graceful degradation

---

## 1.0.0 — Production Ready 🔮

_Not yet broken into issues. This is the vision for what a production release would need._

### Authentication & Authorization
- JWT-based auth or OAuth2/OIDC integration
- Role-based access: ADMIN, OPS_MANAGER, OPS_AGENT, CUSTOMER (read-only)
- API key support for external freight forwarders
- Rate limiting per client

### Audit & Compliance
- Full audit log on all mutations (who changed what, when)
- Soft deletes instead of hard deletes
- Data retention policies
- GDPR-aware customer data handling

### Operational Hardening
- Database migrations via Flyway (replace `ddl-auto=update`)
- Connection pooling tuned for production (HikariCP settings)
- Health checks and readiness probes (`/actuator/health`)
- Structured JSON logging (Logback + ELK or similar)
- Correlation IDs across requests

### Deployment
- Dockerfile for the application (multi-stage build)
- Docker Compose with app + DB + MailHog for full local stack
- CI/CD pipeline (GitHub Actions: build → test → format check → Docker image)
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

_Last updated: February 2026_