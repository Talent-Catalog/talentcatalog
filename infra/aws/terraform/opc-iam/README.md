# OPC IAM Groups and Policies

This document records the IAM user group structure for OPC accounts as part of
issue #983 ("IAM roles - TBB + OPC").

## Scope

The following group and policy model applies to:

- OPC staging account: `164804461258`
- OPC production account: `289896345557`

## Management approach

These IAM groups and user assignments are currently managed in the AWS Console
(not through Terraform).

Rationale:

- The existing Terraform stack in `infra/aws/terraform` is focused on runtime
  application infrastructure (ECS, RDS, networking, etc).
- Human IAM identities (users/groups) are a separate lifecycle and are easier
  to operate safely outside that stack.
- `Managed policies per group` has a hard account quota of 10. This affects how
  Developer permissions are represented (details below).

## Groups

### Administrators

Attached managed policy:

- `AdministratorAccess` (AWS managed)

Notes:

- `AmazonS3FullAccess` is not attached because it is redundant when
  `AdministratorAccess` is present.
- Existing users `john` and `sadat` remain in `Administrators`.

### Developers

Attached managed policies (10):

- `AdministratorAccess-Amplify`
- `AmazonEC2ContainerRegistryFullAccess`
- `AmazonECS_FullAccess`
- `AmazonElastiCacheFullAccess`
- `AmazonRDSFullAccess`
- `AmazonS3FullAccess`
- `AWSCloudTrail_FullAccess`
- `AWSLambda_FullAccess`
- `CloudWatchFullAccess`
- `CloudWatchFullAccessV2`

Inline group policy:

- `DevelopersSelfService`

The inline policy combines the intent of the two customer-managed policies from
the legacy TBB account:

- `DevelopersIAMUserAccess` (self-service IAM on own user/MFA resources)
- `DevelopersMFASetup` (list/create/enable virtual MFA device)

Why inline was used:

- AWS IAM enforces a non-adjustable limit of 10 managed policies per group.
- Developers already requires 10 AWS-managed policies.
- Attaching 2 additional customer-managed policies would exceed the quota.

To keep behavior equivalent, self-service permissions are represented as one
inline policy on `Developers`.

### Operations

Attached managed policies:

- `AWSBillingReadOnlyAccess`
- `AWSHealthFullAccess`
- `AWSSecurityHubFullAccess`
- `AWSServiceCatalogAdminFullAccess`
- `CloudWatchReadOnlyAccess`

## Customer-managed policies created

The following customer-managed policies were created in both OPC accounts:

- `DevelopersIAMUserAccess`
- `DevelopersMFASetup`

Given the managed-policy-per-group quota, they are currently not attached to
`Developers` and their effective permissions are implemented via the
`DevelopersSelfService` inline policy.

## User provisioning process

For each new user:

1. Create IAM user with console access and temporary password.
2. Require password reset at first sign-in.
3. Add user to one or both of: `Developers`, `Operations`.
4. Share initial credentials out-of-band.
5. Require MFA setup on first login.

