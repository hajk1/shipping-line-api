# Contributing

## Picking an Issue

1. Start with **`CRD-001`, `CRD-002`, `CRD-003`** (🟢 Easy) — they are independent and follow the
   existing pattern closely
2. Move to **`CRD-004`** (🟡 Medium) once you're comfortable — it introduces more business logic
3. **`ENH-001`, `ENH-002`** (🟡 Medium) can be done in parallel by different people
4. **`ENH-003`, `ENH-004`** (🟠 Challenging) depend on earlier issues and require deeper thinking

## Workflow

1. **Assign yourself** to the issue so others know it's taken
2. **Create a feature branch** from `master`:
   ```bash
   git checkout master
   git pull
   git checkout -b feature/CRD-001-port-crud
   ```
3. **Do your work** — follow the patterns in `FreightOrderController`
4. **Run checks before pushing:**
   ```bash
   mvn clean verify   # builds + tests + coverage report
   mvn fmt:check      # verifies Google code style
   ```
   CI runs the same checks automatically — but catching issues locally saves time.
5. **Push and open a PR:**
   ```bash
   git push origin feature/CRD-001-port-crud
   ```
6. **PR title format:** `CRD-001 — Add Port CRUD controller`
7. **Request a review** and address feedback

## Branch Naming

| Type    | Pattern                         | Example                             |
|---------|---------------------------------|-------------------------------------|
| Feature | `feature/DOMAIN-NNN-short-name` | `feature/CRD-004-voyage-controller` |
| Bugfix  | `fix/DOMAIN-NNN-short-name`     | `fix/ENH-004-double-booking`        |
| Chore   | `chore/short-name`              | `chore/update-readme`               |

## Commit Messages

Keep them short and descriptive. Prefix with the issue number:

```
CRD-001 add CreatePortRequest DTO and validation
CRD-001 add PortService and PortController
CRD-001 add PortControllerTest
```

## Code Style

This project enforces [Google Java Format](https://github.com/google/google-java-format). The build auto-formats your code, but you can also run it manually:

```bash
mvn fmt:format    # auto-format
mvn fmt:check     # check without changing
```

Set up your IDE plugin so you don't have to think about it:
- **IntelliJ:** Install the "google-java-format" plugin → Settings → Enable
- **VS Code:** Install the "Google Java Format" extension

## What Makes a Good PR

- **One issue per PR** — don't bundle unrelated changes
- **Tests included** — follow `FreightOrderControllerTest` as a template
- **Coverage maintained** — CI posts a coverage comment on your PR. Aim for 60%+ on changed files
- **DTO layer respected** — never expose JPA entities directly in responses
- **Formatting clean** — `mvn fmt:check` must pass
- **Small and reviewable** — if it's getting big, break it up