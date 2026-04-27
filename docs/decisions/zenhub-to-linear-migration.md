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
