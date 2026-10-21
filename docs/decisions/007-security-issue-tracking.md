# ADR 007: Security Issue Tracking — Linear as Source of Truth, Severity Mapped to Vanta

**Date:** 2026-10-19
**Status:** Decided

## Context

Vanta monitors how we handle security issues through a group of task-tracker tests:
every security issue must have an owner and a priority, and each priority level must be
resolved within its SLA (the P0, P1, P2 and P3+ "security issues resolved" tests).
Vanta treats any issue carrying one of its configured security labels as a security
issue.

Several things made these tests unreliable:

- **Two copies of every issue.** The Linear Engineering team automatically mirrors new
  issues to GitHub issues in the public `talentcatalog` repository. Vanta's GitHub
  integration read those mirrors as a second set of security issues. The mirrors don't
  carry Linear's assignee or priority, so they failed the ownership and priority tests
  however well the Linear issues were maintained.
- **No agreed meaning for each priority.** Issues were prioritised case by case, with no
  written definition of what High or Medium means. That made the SLA tests hard to
  defend: an issue's priority, and therefore its deadline, depended on who raised it.
- **SLAs that disagreed with policy.** The Operations Security Policy (Technical
  vulnerability management) sets remediation timeframes of 30 days for Critical and
  High, 60 days for Medium and 90 days for Low. Vanta's security ticket SLAs were 7, 14,
  90 and 90 days. Medium was looser than the policy allows, and Critical and High were
  stricter than the policy requires.

The Operations Security Policy has been extended to cover security issues generally,
not just vulnerabilities, with written definitions for each severity level. This ADR
records how that policy is implemented in Linear and Vanta.

## Decision

1. **Linear is the system of record for security issues.** Security issues are created,
   prioritised, assigned and resolved in Linear. The GitHub mirrors are not tracked as
   security issues.
2. **GitHub mirrors are excluded from Vanta's task tracking.** The GitHub integration's
   task tracking label is set to `vanta-github-ignore`, a label never applied to any
   issue. The Linear integration keeps the labels `security` and `Security`.
3. **Severity follows the Operations Security Policy.** The policy is the authority for
   severity definitions and remediation timeframes. Linear priorities map onto it as
   follows:

   | Policy severity | Linear priority | Vanta level | Remediation SLA |
   |---|---|---|---|
   | Critical | Urgent | P0 | 30 days |
   | High | High | P1 | 30 days |
   | Medium | Medium | P2 | 60 days |
   | Low | Low | P3+ | 90 days |
   | Informational | Not tracked as a security issue | — | As needed |

   Vanta counts issues with no priority as P3+. We don't rely on that default (see the
   rules below).
4. **Vanta's security ticket SLAs match the policy.** On 2026-10-19 they were changed
   from 7 / 14 / 90 / 90 days to 30 / 30 / 60 / 90 days. Vanta's vulnerability and
   security alert SLAs already used these values and were left unchanged.
5. **Rules for security issues:**
   - Every security issue gets an owner and a priority when it is created.
   - A priority change is recorded in a comment on the issue, with the reason.
   - Only genuine security issues get the Security label. Process, tooling and
     tech-debt issues don't, even when they come out of compliance work.
   - An issue that can't be resolved within its SLA follows the policy's exceptions
     route: a risk treatment plan and planned remediation timeline, recorded on the
     issue.
6. **Sensitive findings stay out of synced issues.** Because the Engineering team mirrors
   issues to a public repository, a finding that describes a live, unfixed weakness in
   production is not recorded in a synced Linear issue until it is fixed. It is tracked
   privately, and a general ticket is raised for the remaining work once the risk is
   removed.

## Reasoning

### One source of truth

Linear is where the team plans and does the work, and it holds the assignee and priority
fields Vanta needs. The GitHub mirrors exist for public visibility, not for tracking.
Counting both would double every issue and leave half of them permanently failing.

### Excluding GitHub by label, not by removing labels

Removing the Security label from the GitHub mirrors doesn't last, because the sync
re-applies it. Deactivating each GitHub entry in the failing tests works once, but every
newly mirrored issue creates a new entry. Setting the GitHub integration's task tracking
label to a label that is never used stops Vanta treating GitHub issues as security tasks,
while it keeps reading GitHub for repositories, branch protection, pull requests and
Dependabot.

### The policy is the authority, Vanta enforces it

Auditors check practice against approved policy. Keeping the definitions and SLAs in
the policy, and configuring Vanta to match, means there is one set of commitments. Vanta's
tests then measure exactly what the policy says, rather than a separate, unapproved
standard.

### Definitions based on impact, not effort

Each level is defined by the impact of the weakness and how directly it could be
exploited, not by how hard it is to fix. That keeps the most urgent list for the most
serious risks, and makes each priority defensible to an auditor.

### Recorded priority changes

Re-rating issues against the new definitions is expected, and some issues will move
down. A recorded reason on each change shows the re-rating was done against the
definitions, not to make a test pass.

## Consequences

- The "security issues assigned" and "security issues prioritised" tests depend only on
  Linear. Both passed once the GitHub mirrors were excluded.
- Vanta applies SLA changes to new issues only. Issues already open on 2026-10-19 keep
  the deadlines from the previous SLAs until they are resolved, then later issues follow
  the new values. Some issues will therefore show as overdue against the old, stricter
  P1 SLA.
- Any future change to remediation timeframes must be made in the Operations Security
  Policy and in Vanta's SLA settings together, and noted here.
- When the SLAs were changed, the P3+ SLA was found disabled and was re-enabled at 90
  days. After any SLA change, check that all four security ticket SLAs show as enabled
  with the expected values.
- If a new task tracker is connected to Vanta, its task tracking labels must be set
  deliberately: either the security labels, if it is a real tracker, or
  `vanta-github-ignore`-style exclusion, if it only mirrors Linear.
- Open security issues need re-rating against the new definitions, each with a comment
  explaining any change.

## Alternatives Considered

| Option | Outcome                                                                                                                                             |
|---|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| Use GitHub issues as the tracker | Rejected: the team works in Linear, and GitHub issues have no priority field for Vanta to read                                                      |
| Remove the security labels from Vanta's task tracking settings for all trackers | Rejected: Vanta would then track no security issues at all, passing the tests for the wrong reason                                                  |
| Deactivate each GitHub mirror in the failing tests | Rejected as a permanent fix: every newly mirrored issue needs deactivating again                                                                    |
| Keep Vanta's previous SLAs (7 / 14 / 90 / 90 days) | Rejected: Medium was looser than the approved policy, and Critical and High were stricter than policy requires                                      |
| Tighten the policy to match Vanta's previous SLAs | Not adopted: the policy's timeframes already matched Vanta's vulnerability SLAs, so aligning the ticket SLAs to the policy kept one consistent scale |
| A separate, non-synced Linear team for sensitive findings | Not adopted for now: sensitive findings are kept out of Linear until fixed instead. To be revisited if their number grows                           |
