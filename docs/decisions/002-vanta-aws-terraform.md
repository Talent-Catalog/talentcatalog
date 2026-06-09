# ADR 002: Vanta AWS Integration — Terraform for OPC/GRN, Console for TBB

**Date:** 2026-05-26
**Status:** Accepted

## Context

We are integrating our AWS accounts with Vanta to support continuous compliance
monitoring (SOC 2, ISO 27001). The integration requires provisioning a read-only
IAM role (`vanta-auditor`) that Vanta's scanner can assume.

Our AWS estate at the time of integration consists of three accounts:

- **OPC prod account** (`289896345557`) — managed by Terraform in
  [`infra/aws/terraform/opc-prod/`](infra/aws/terraform/opc-prod/). Also used by
  `grn-prod`, which shares the same account with separate Terraform state.
- **OPC staging account** (`164804461258`) — managed by Terraform in
  [`infra/aws/terraform/opc-staging/`](infra/aws/terraform/opc-staging/). Also used
  by `grn-staging`.
- **TBB prod account** — a legacy standalone account. A
  [`infra/aws/terraform/prod/`](infra/aws/terraform/prod/) directory exists but TBB
  prod infrastructure has **not** been placed under Terraform management. The
  intention is for TBB to eventually migrate into the OPC prod account.

When this decision was first drafted, three provisioning options were considered:
CloudFormation, Terraform (community module), and manual console setup. CloudFormation
was initially favoured due to Vanta owning its maintenance and the absence of a
viable first-party Terraform option.

On commencing the integration, it became clear that Vanta now provides **first-party
Terraform support** directly within their setup flow ("We'll provide a Terraform
script you can add to your existing infrastructure-as-code"). This materially changes
the trade-off and prompted the decision for Terraform.

## Decision

- **OPC prod and OPC staging accounts** are connected to Vanta using Vanta's
  first-party Terraform script, committed verbatim to the repository.
- **TBB prod account** is connected via the AWS console, as TBB prod infrastructure
  is not under Terraform management and is expected to migrate to the OPC prod
  account in future.
- `grn-prod` and `grn-staging` do not require separate Vanta connections because
  they share AWS accounts with `opc-prod` and `opc-staging` respectively. One
  `vanta-auditor` role per account covers all deployments in that account.
- Each account is connected in Vanta as an **individual account** with its own
  connection-specific `external_id` (baked into the script Vanta provides).
- Both **staging and production** environments are monitored.

## Reasoning

### Vanta now provides first-party Terraform support
When this decision was first considered, no official Terraform path was visible from
Vanta. The only community module (`highwingio/vanta/aws`) had been archived in July
2025. However Vanta's setup flow provides a Terraform script directly, meaning Vanta
owns the canonical permission definition. A maintenance concern that originally
favoured CloudFormation no longer applies for the Terraform-managed accounts.

### Use Vanta's script verbatim
Vanta generates a unique `external_id` and embeds it directly in the Terraform
script they provide. There is no separate step to obtain or supply the external ID —
Vanta knows its value on their side. The script is therefore used exactly as
provided, with no additional variables or lifecycle rules added. This keeps the
integration simple and means future script updates from Vanta can be applied
without adaptation.

### Consistency with existing infrastructure-as-code
For the two OPC accounts, all infrastructure is managed in Terraform. Using Terraform
for the Vanta integration keeps the entire estate in one toolchain, making it easier
to onboard new engineers, reason about state, and conduct reviews.

### TBB prod via console is pragmatic
TBB prod is a legacy account not under Terraform management and subject to future
migration. Introducing Terraform management of a single IAM role in an otherwise
unmanaged account would create a misleading impression of Terraform coverage. The
console connection is appropriate for its transient status.

### Multi-account coverage
One `vanta-auditor` role per AWS account is sufficient. Because `grn-prod` and
`grn-staging` run in the same accounts as `opc-prod` and `opc-staging`, a single
Vanta connection per account covers resources from all deployments in that account.

### Monitor staging as well as production
Both staging and production environments are monitored. The primary compliance
obligations relate to production, but monitoring staging provides early warning of
misconfigurations before they are replicated to production. Vanta's scoping and
labelling features should be used to distinguish production-critical findings from
staging-informational ones.

## Consequences

- `vanta.tf` files using Vanta's provided script verbatim are committed for the two
  Terraform-managed accounts:
  - `infra/aws/terraform/opc-prod/vanta.tf`
  - `infra/aws/terraform/opc-staging/vanta.tf`
- The TBB prod connection is managed outside Terraform via the AWS console. It will
  be superseded when TBB prod migrates into the OPC prod account.
- When Vanta updates their required permissions, they will provide an updated
  Terraform script. The update should be applied via the normal Terraform review
  and apply workflow for the OPC accounts.
- The `VantaAdditionalPermissions` custom policy contains two statements: an `Allow`
  block for `identitystore:*` actions (required for IAM Identity Center scanning)
  and a `Deny` block for `datapipeline` and `rds` log download actions.
- Vanta monitoring is enabled for both staging and production. Findings should be
  triaged with environment context in mind.

## Alternatives Considered

| Option | Outcome |
|---|---|
| CloudFormation (Vanta template) | Rejected for OPC accounts: exists outside Terraform state; harder to scope per environment; mixed toolchain |
| AWS console | Used for TBB prod only, due to TBB not being under Terraform management |
| `highwingio/vanta/aws` Terraform module | Rejected: archived July 2025, no longer maintained |
| Cloud Posse `aws-vanta` component | Rejected: requires adopting Cloud Posse conventions; disproportionate for a single integration |
