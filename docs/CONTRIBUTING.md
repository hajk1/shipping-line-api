# Contributing

## Finding an Issue

Issues are organised into three phase files at the repository root:

| File               | Content                                               |
|--------------------|-------------------------------------------------------|
| `ISSUES.md`        | Phase 1 — Core CRUD and foundations ✅ Complete        |
| `ISSUES-PHASE2.md` | Phase 2 — Pricing, invoicing, tracking, AI ✅ Complete |
| `ISSUES-PHASE3.md` | Phase 3 — Infrastructure hardening + bulk cargo 🔧    |

Pick an open item from `ISSUES-PHASE3.md`. The dependency order and suggested starting points are
listed at the bottom of that file.

## Workflow

1. **Assign yourself** to the issue so others know it's taken
2. **Create a feature branch** from `master`:
   ```bash
   git checkout master
   git pull
   git checkout -b feature/ENH-006-vessel-response-id
   ```
3. **Do your work** — follow the patterns in `FreightOrderController` and
   `FreightOrderControllerTest`
4. **Run checks before pushing:**
   ```bash
   ./mvnw clean verify   # build + test + coverage report
   ./mvnw fmt:check      # verify Google code style
   ```
5. **Push and open a PR:**
   ```bash
   git push origin feature/ENH-006-vessel-response-id
   ```
6. **PR title format:** `ENH-006 — Expose id on VesselResponse and VoyageResponse`
7. **Request a review** and address feedback

## Branch Naming

| Type    | Pattern                         | Example                             |
|---------|---------------------------------|-------------------------------------|
| Feature | `feature/DOMAIN-NNN-short-name` | `feature/INF-001-flyway-migrations` |
| Bugfix  | `fix/DOMAIN-NNN-short-name`     | `fix/ENH-007-not-found-returns-500` |
| Chore   | `chore/short-name`              | `chore/update-readme`               |

## Commit Messages

Prefix with the issue code:

```
INF-001 add flyway-core dependency and ddl-auto=validate
INF-001 add V1__baseline.sql covering all current tables
INF-001 verify H2 compatibility in test suite
```

## Code Style

This project enforces [Google Java Format](https://github.com/google/google-java-format).

```bash
./mvnw fmt:format    # auto-format
./mvnw fmt:check     # check without changing (same as CI)
```

**IDE setup:**

- **IntelliJ:** Install the "google-java-format" plugin → Settings → Enable; enable annotation
  processing for Lombok
- **VS Code:** Install "Google Java Format" and "Lombok Annotations Support" extensions

## What Makes a Good PR

- **One issue per PR** — don't bundle unrelated changes
- **Tests included** — follow `FreightOrderControllerTest` as a template; happy path AND at least
  one negative case (404 / 400)
- **Coverage maintained** — CI posts a coverage comment; aim for 60%+ on changed files
- **DTO layer respected** — never expose JPA entities directly in responses
- **No lazy-load traps** — service methods that access associations must be `@Transactional`
- **Not-found as 404** — throw `ResponseStatusException(NOT_FOUND)`, not `IllegalArgumentException`
- **Formatting clean** — `./mvnw fmt:check` must pass
- **Small and reviewable** — if it's getting large, break it into smaller PRs
