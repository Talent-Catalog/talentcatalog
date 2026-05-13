# ZenHub to Linear Migration — Decision Record

**Project:** Talent Catalog  
**Date:** April 2026  
**Status:** In progress

This document records the key decisions made during the migration from ZenHub to Linear as the team's product management platform. Each decision section states the options considered, the recommendation, and the outcome once agreed.

---

## Decision 1: Issue Tracking — Source of Truth

**Status:** Decided

### Context

Linear requires a decision on where issues are filed and managed. ZenHub layers over GitHub Issues, so today GitHub Issues are the canonical source. Moving to Linear means choosing whether Linear replaces GitHub Issues or complements them.

### Options

**Option A — Linear as sole source of truth**  
Issues live exclusively in Linear with Linear IDs. GitHub Issues are disabled. This is a clean single-system architecture but is incompatible with open-source contribution: external contributors have no way to file bugs or feature requests.

**Option B — Hybrid (GitHub Issues + Linear)**  
GitHub Issues remain the canonical issue tracker for all contributors — core engineers and external contributors alike. Linear sits on top as a product planning layer, providing roadmaps, epics, and cycles, with a two-way sync to GitHub. Engineers and external contributors continue working from GitHub Issues unchanged.

### Decision

**Option B — Hybrid.**

Option A is not viable. Talent Catalog is an open-source project; the GitHub Issues tab is the universal contract between the project and its contributor community. Disabling it would cut off external contributions and violate the expectations of any developer who finds the repository.

Option B is not a compromise — it is the correct architecture. Linear addresses the product management limitations of ZenHub without disrupting the engineering workflow or the open-source contributor experience.

---

## Decision 2: Branch Naming Convention

**Status:** Pending

### Context

The team's branch naming convention (`{github-issue-number}-description`, e.g. `3353-hardcoded-default-password`) is shown by example in `CONTRIBUTING.md`.

This decision applies only to the core team. External contributors work from forks and name branches as they choose; the convention is guidance, not enforcement.

### Options

**Option A — Keep GitHub issue numbers**  
No change. Branches continue to use the GitHub issue number as the prefix. Linear traceability is handled via PR linking.

**Option B — Adopt Linear IDs**  
Branches use the Linear-generated format (`{dev}/{linear-id}-description`, e.g. `sadat/tc-123-fix-password-validation`). Linear's "copy branch name" button generates this automatically.

### Decision

_To be agreed._

---

## Decision 3: Historical ZenHub Data

**Status:** Decided

### Context

ZenHub stores sprint history, velocity data, and burndown charts. This data does not export cleanly to Linear and cannot be carried over.

### Options

**Option A — Export and archive**  
Export ZenHub data as CSV before decommissioning and archive externally (e.g. in `docs/` or a shared drive). Accept that Linear's velocity tracking will start fresh.

**Option B — Discard**  
Make no effort to preserve historical data. ZenHub data is lost on decommissioning.

### Decision

**Option A — Export and archive.**

ZenHub's sprint and velocity tracking has not been a strong feature of the tool in practice, so the loss of in-app history is minimal. Archiving preserves the record for completeness at negligible effort. Linear's velocity metrics will reset and stabilise over the first 3–4 cycles.

---

## Decision 4: Workspace and Team Structure

**Status:** Pending

### Context

Linear is organised as a workspace containing one or more teams. Each team has its own issue ID prefix (e.g. `TC-123`), workflow states, and cycles. The choice affects how product planning, roadmaps, and reporting are scoped.

### Options

**Option A — Single team**  
One workspace, one team (`Talent Catalog`). All issues share a single prefix. Simpler day-to-day; product planning is scoped across the whole project.

**Option B — Multiple teams**  
One workspace, multiple teams (e.g. `Backend`, `Frontend`, `Platform`). Each team has its own prefix, cycles, and roadmap. Allows independent planning per stream at the cost of added structural complexity.

### Decision

_To be agreed._

**Recommendation:** Start with Option A. Split into multiple teams later if product management identifies a clear need to plan streams independently.