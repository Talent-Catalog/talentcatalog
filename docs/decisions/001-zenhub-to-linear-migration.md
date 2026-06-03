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

**Status:** Decided

### Context

The team's branch naming convention (`{github-issue-number}-description`, e.g. `3353-hardcoded-default-password`) is shown by example in `CONTRIBUTING.md`.

This decision applies only to the core team. External contributors work from forks and name branches as they choose; the convention is guidance, not enforcement.

Under the hybrid model (Decision 1), every GitHub issue is mirrored in Linear, so a Linear issue always exists as the starting point for branch creation.

### Options

**Option A — Keep GitHub issue numbers**  
Branches continue to use the GitHub-generated format (`{github-issue-number}-description`). No change to existing convention. Linear traceability requires a manual step — developers must include the Linear ID (e.g. `Fixes TC-123`) in the PR title or description; it is not automatic.

**Option B — Adopt Linear IDs**  
Branches use the Linear-generated format (`{username}/{linear-id}-description`, e.g. `john/tc-123-fix-password-validation`). Linear's `Cmd/Ctrl Shift .` shortcut copies the branch name, moves the issue to In Progress, and self-assigns — in one step. The Linear ID in the branch name auto-links the PR to the Linear issue on creation, with no manual step required.

### Decision

**Option B — Linear-generated branch names.**

Auto-linking from branch → PR → Linear issue is automatic and reliable. Option A requires developers to manually add `Fixes TC-123` to every PR — a discipline that tends to be inconsistently applied over time. The habit shift (starting from a Linear issue rather than a GitHub issue) is modest; every GitHub issue is mirrored in Linear so the starting point always exists.

`CONTRIBUTING.md` will need updating to reflect the new convention. External contributors are unaffected — branch naming is guidance, not enforced.

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

**Status:** Decided

### Context

Linear is organised as a workspace containing one or more teams. Each team has its own issue ID prefix (e.g. `TC-123`), workflow states, and cycles. The choice affects how product planning, roadmaps, and reporting are scoped.

### Options

**Option A — Single team**  
One workspace, one team (`Talent Catalog`). All issues share a single prefix. Simpler day-to-day; product planning is scoped across the whole project.

**Option B — Multiple teams**  
One workspace, multiple teams (e.g. `Backend`, `Frontend`, `Platform`). Each team has its own prefix, cycles, and roadmap. Allows independent planning per stream at the cost of added structural complexity.

### Decision

Start with Option A. Split into multiple teams later if product management identifies a clear need to plan streams independently.

---

## Decision 5: Workflow States

**Status:** Decided

### Context

Linear teams have a configurable set of workflow states that issues move through. Linear ships with a default set; the team can adopt those defaults or define a custom set tailored to the existing development process.

### Options

**Option A — Linear defaults**  
Use Linear's out-of-the-box states: `Backlog`, `Todo`, `In Progress`, `In Review`, `Done`, `Canceled`, `Duplicate`. Minimal setup; consistent with other Linear users joining the team.

**Option B — Custom set aligned to current process**  
Adopt a state set that mirrors the existing ZenHub pipeline and adds a `Triage` state for open-source intake:

| State       | Type      | Equivalent                                  |
|-------------|-----------|---------------------------------------------|
| Triage      | Triage    | New GitHub issues needing maintainer review |
| Backlog     | Backlog   | Approved, not yet scheduled                 |
| Todo        | Unstarted | Pulled into current cycle                   |
| In Progress | Started   | Active development                          |
| In Review   | Started   | PR open, awaiting review                    |
| Done        | Completed | Merged to staging                           |
| Closed      | Closed    | Closed, rejected, duplicate or won't-do     |

### Decision

**Option A — Linear defaults.**

Issue triage is handled ad-hoc; a dedicated `Triage` state would add process overhead without matching how the team actually works. The default set covers the workflow cleanly and requires no custom configuration.

---

## Decision 6: Labels

**Status:** Decided

### Context

Linear's GitHub Issues Sync syncs labels bidirectionally along with title, description, status, assignee, and comments. Once sync is enabled, the existing GitHub label set becomes the Linear label set automatically; labels applied in either system propagate to the other. There is effectively one shared label taxonomy across both systems.

The current GitHub label set has accumulated over time. It contains structurally meaningful labels (e.g. `bug`, `enhancement`, `hotfix`, `tech debt`, `blocked`) alongside several that may be stale, overlapping, or inconsistently used.

Priorities are not in scope here. Linear's built-in priority system (`Urgent` / `High` / `Medium` / `Low` / `No Priority`) is adopted as-is; there is no meaningful alternative.

### Options

**Option A — Adopt the existing labels as-is**  
Enable sync without modification. The current GitHub label set becomes the Linear label set. No clean-up effort, but inherits existing label drift and duplication.

**Option B — Tidy up before sync**  
Audit and consolidate the existing GitHub labels before enabling Linear sync: retire stale labels, merge overlapping ones, rename inconsistent ones. The cleaner set then propagates into Linear.

### Decision

Option A. The existing label set is serviceable. A clean-up effort would require manual review of all labels, decisions on which to keep/merge/retire. We have imported the full labels set, noting a ticket to tidy up later if it becomes a pain point.

---

## Decision 7: Cycle Cadence and Cool-down

**Status:** Decided

### Context

Linear calls sprints "Cycles". Each cycle has two configurable dimensions, and crucially they are **separate periods**, not nested:

- **Cycle** — the active development period during which issues are assigned and worked on.
- **Cool-down** — an optional, unscheduled period **between** cycles. Issues cannot be assigned to a cool-down; it is dedicated time for retro, planning, tech debt, and smaller cross-cutting work.

The cool-down concept does not exist in ZenHub sprints and is new for the team. Because cool-down is not part of the cycle, choosing to enable it lengthens the overall delivery cadence unless cycle length is reduced to compensate.

The team currently runs 3-week sprints in ZenHub.

### Options

| Option | Cycle | Cool-down | Effective cadence | Notes |
|---|---|---|---|---|
| A | 3 weeks | 0 (disabled) | 3 weeks | Mirrors current ZenHub rhythm exactly. Simple and predictable, but loses the benefit of structured cool-down. |
| B | 3 weeks | 1 week | 4 weeks | Adds breathing room, but ~25% fewer cycles per year than today. |
| C | 2 weeks | 1 week | 3 weeks | Preserves the overall 3-week cadence but with shorter active work blocks. |

### Decision

**Option C — 2-week cycle, 1-week cool-down.**

Preserves the team's existing 3-week effective cadence while introducing structured cool-down time for retro, planning, and tech debt. Cycles start Monday, auto-creating 2 cycles ahead. Both dimensions can be revisited later without losing historical cycle data.

---

## Decision 8: Projects and Initiatives

**Status:** Pending

### Context

Linear provides two organisational layers above individual issues:

- **Projects** — collections of related issues sharing a goal and target date. Direct equivalent to ZenHub epics.
- **Initiatives** — higher-level strategic groupings of multiple projects.

ZenHub epics are in active use today. ZenHub also offers an Initiatives feature, but its implementation is clunky compared to Linear's, and it has not been meaningfully adopted by the team. The migration is effectively a clean slate for using Initiatives in practice.

Importantly, Initiatives are not just optional decoration — they are the strategic grouping layer that organises Linear's roadmap view. Without Initiatives, the roadmap is flat (a timeline of projects with no higher-level structure). With Initiatives, the roadmap shows projects grouped by strategic theme, which is the view most useful for product-level planning.

(Note: Linear's separate **Releases** feature is unrelated to Initiatives. Releases track actual software deployments via CI/CD integration and answer "what is live in production?" — they are a deployment artefact, not a planning one.)

### Options

**Option A — Projects only (initially)**  
Use Projects as a 1:1 replacement for ZenHub epics. Defer Initiatives until product management identifies a clear need and groupings for a higher-level layer.

**Option B — Projects and Initiatives from the start**  
Adopt both layers immediately. Use Initiatives to express longer-term strategic themes that span multiple projects and cycles, for example:

- _Service partnership ecosystem_
- _Global scale and resilience_
- _Trust, privacy and consent_
- _Candidate matching intelligence_

### Decision

_To be agreed._

**Recommendation:** Option B. Improved roadmap capability is a primary driver for the migration, and Initiatives are the layer that gives Linear's roadmap its strategic structure. Deferring them would result in a flat roadmap that fails to deliver one of the main benefits of moving away from ZenHub. Starting with a small, well-chosen set of Initiatives gives product management a meaningful roadmap from day one.

---

## Decision 9: Access Control and Role Assignment

**Status:** Partially decided

### Context

Linear has three role levels: **Admin**, **Member**, and **Guest**. All three are billed as paid seats; Linear bills per unsuspended user regardless of role.

Plan-dependent availability is important:

- **Guest role requires the Business plan ($16/user/month) or Enterprise.** It is not available on Free or Basic.
- On Basic, everyone with access is a Member (or Admin) — there is no read-only / scoped option for stakeholders.

Two questions of principle need to be agreed:

1. How permissively or strictly to assign roles within the core team.
2. Whether the need for Guest access for non-technical stakeholders is significant enough to require the Business plan.

### Decision

**Role assignment principles**

| Role | Assigned to | Rationale |
|---|---|---|
| Admin | Core maintainers (a small subset of the team) | Manage workspace settings, integrations, members, and teams. |
| Member | All core developers with regular commit activity | Full ability to create and manage issues, participate in cycles, update states. |
| Guest | Product or non-technical stakeholders requiring visibility but not full participation | Read + comment access on specific Projects or teams. **Requires Business plan.** |

Named role assignments are operational and will be maintained outside this record.

**External open-source contributors**

External contributors are **not** invited to Linear, even as Guests. They continue to interact exclusively through GitHub Issues and PRs. The GitHub ↔ Linear sync handles the bridge.

Rationale: the contributor pool is open-ended (anyone on GitHub) and cannot be managed at scale through Linear's per-seat model. GitHub Issues remains the public-facing contributor interface (consistent with Decision 1).

---

## Decision 10: GitHub ↔ Linear Sync Direction

**Status:** Decided

### Context

Linear offers two GitHub sync modes ([docs](https://linear.app/docs/github-to-linear#github-sync-options)):

- **Unidirectional** — Issues flow only GitHub → Linear. Linear-only issues stay in Linear. Comments, status, and images sync both ways for issues that exist in both systems.
- **Bidirectional** — Issues, comments, status, images, and deletions flow both ways regardless of where the issue originated.

### Decision

**Bidirectional sync.**

### Rationale

- **Transparency-by-default.** GitHub remains canonical (Decision 1) and publicly visible. The team wants every Linear-created issue to also appear in GitHub so external contributors see the full picture.
- **No private layer.** A class of Linear-only issues invisible to external contributors would conflict with the project's open-source intent.
- **Image sync for all issues.** Bidirectional ensures images sync in both directions for all issues — an improvement over ZenHub.

---

## Decision 11: GitHub Repository Sync Configuration

**Status:** Decided

### Context

Linear's bidirectional GitHub Issues Sync is limited to one repository per Linear team. However, Linear also supports unidirectional sync (GitHub → Linear only) for additional repositories. The organisation has four active repositories:

- `Talent-Catalog/talentcatalog` — main monorepo, primary issue home
- `Talent-Catalog/tc-api`
- `Talent-Catalog/tc-api-spec`
- `Talent-Catalog/tc-skills-extraction-service`

### Decision

**Bidirectional sync for `talentcatalog`; unidirectional (GitHub → Linear) for the remaining three repos.**

This configuration means:
- Issues filed in any of the four repos on GitHub automatically appear in Linear.
- Linear-created issues sync to `talentcatalog` on GitHub (consistent with Decision 10).
- No manual bridging or issue consolidation is required.

The only asymmetry is that Linear-created issues appear in `talentcatalog` rather than a service-specific repo. This is acceptable given that `talentcatalog` is the primary and canonical issue home for the project.

---

## Decision 12: PR Merge Automation

**Status:** Decided

### Context

Linear can automatically move issues to `Done` when a linked PR or commit is merged.

### Decision

Set **On PR or commit merge, move to...** to **No action**.

### Rationale

A merged PR is necessary but not sufficient for an issue to be complete. Engineers must complete issue sign-off criteria before moving the issue to `Done`, including release notes, release planning, user notification, and documentation updates.

---

## Decision 13: Issue Templates

**Status:** Decided

### Context

Without templates, issue quality and completeness vary. Sign-off expectations can be missed if they are not present at issue creation.

### Decision

Adopt four templates for all new issues:

- Feature
- Bug
- Security
- Tech Debt

Maintain these templates in both places:

- GitHub templates in `.github/ISSUE_TEMPLATE/` (source of truth, version-controlled)
- Matching templates in Linear team settings

Source-of-truth template files:

- `.github/ISSUE_TEMPLATE/feature.md`
- `.github/ISSUE_TEMPLATE/bug.md`
- `.github/ISSUE_TEMPLATE/security.md`
- `.github/ISSUE_TEMPLATE/tech_debt.md`

### Rationale

GitHub templates guide external contributors creating issues directly in GitHub. Linear templates guide core team members creating issues in Linear. Keeping content aligned enforces consistent issue quality regardless of entry point.
