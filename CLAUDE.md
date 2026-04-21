# Claude Working Instructions

## Default Workflow

### 1. Think before acting
When the user raises a topic or problem, **think and plan first**. Do not immediately write code.

### 2. Create a ticket, not a fix
Unless the request is marked **urgent**, respond by:
- Briefly describing the problem and root cause
- Drafting a ticket in `docs/ISSUES-PHASE3.md` (or the appropriate phase file) following the existing format
- Stopping there — do not implement

### 3. Get approval before implementing
When ready to implement a ticket (urgent or approved), **brief the solution first**:
- What will change (files, entities, queries, tests)
- Any trade-offs or risks
- Wait for explicit approval before writing code

### 4. Test-driven development
Once approved, write **failing tests first** — only the tests necessary to prove correctness of the specific change, no more. Then implement until they pass.

---

## What counts as urgent
The user explicitly says: "do it now", "fix this", "urgent", or pastes a live error trace expecting an immediate fix.

## Ticket format
Follow the structure in `docs/ISSUES-PHASE3.md`: description paragraph, named sections, `**Acceptance criteria:**` checklist.
