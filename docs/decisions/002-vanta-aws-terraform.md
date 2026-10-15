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

## Addendum: CloudWatch alarms for TBB prod (TC-1526)

**Date:** 2026-10-15
**Status:** Accepted
**Issue:** TC-1526 (GitHub #3841)

### Context

Vanta's CloudWatch alarm tests require every Application Load Balancer, RDS
instance and Lambda function in each connected AWS account to have an alarm on
specific metrics, with notifications going to a monitored destination. Eight tests
were failing across all three accounts.

In the OPC prod and OPC staging accounts, and for the Terraform-managed services in
TBB prod (`tc-api`, `tc-skills-extraction`), alarms were added to each repository's
shared Terraform module:

- `talentcatalog`: `infra/aws/terraform/monitoring.tf`
- `tc-api`: `infra/terraform/monitoring.tf`
- `tc-skills-extraction-service`: `infra/terraform/monitoring.tf`

Three TBB prod resources are not under Terraform management, consistent with this
ADR: the `lb-tbb-service` load balancer, the `prod-tbb` RDS instance and the
`ECSServiceRedeployFunction` Lambda function. TBB prod also needed an SNS topic for
alarm notifications.

### Decision

- The SNS topic and the alarms for the three unmanaged TBB prod resources are
  created **manually** (console and AWS CLI), not in Terraform. This is a deliberate
  exception to TC-1526's "no alarms outside Terraform" criterion, applying the same
  reasoning as the TBB prod Vanta connection above.
- Every manual resource is recorded below so it can be audited, recreated, or
  brought under Terraform later.
- When TBB prod migrates into the OPC prod account, these resources are replaced by
  Terraform-managed equivalents under the `opc-prod` root, and the manual ones are
  deleted.

### Manual resources in TBB prod

All resources are in account `968457613372`, region `us-east-1`. The alarms are
tagged `ManagedBy = manual` and `Ticket = TC-1526`.

#### SNS topic

| Setting | Value |
|---|---|
| Name | `tbb-prod-alarms` |
| ARN | `arn:aws:sns:us-east-1:968457613372:tbb-prod-alarms` |
| Type | Standard |
| Encryption | None (see Consequences) |
| Access policy | Default (same-account publishers) |
| Subscription | Email: the team's AWS alerts Google Group (address recorded in private documentation) |
| Confirmation | AWS CLI with `--authenticate-on-unsubscribe true` |

The topic name is a contract: the Terraform roots `tc-api-prod` (repo `tc-api`) and
`tc-skills-service-prod` (repo `tc-skills-extraction-service`) look it up by name
with `data "aws_sns_topic"`. Renaming or deleting it breaks their plans.

#### Alarms

Every alarm sends both `alarm_actions` and `ok_actions` to `tbb-prod-alarms`.

| Alarm name | Namespace / metric | Dimensions | Statistic | Period | Threshold | Datapoints | Missing data |
|---|---|---|---|---|---|---|---|
| `lb-tbb-service-prod-alb-unhealthy-hosts` | `AWS/ApplicationELB` `UnHealthyHostCount` | LoadBalancer `app/lb-tbb-service/6e11f5079ee133ac`, TargetGroup `tg-any-ip-HTTP8080` | Maximum | 60s | > 0 | 3 of 5 | notBreaching |
| `lb-tbb-service-prod-alb-latency-p95` | `AWS/ApplicationELB` `TargetResponseTime` | LoadBalancer | p95 | 60s | > 2s | 5 of 5 | notBreaching |
| `lb-tbb-service-prod-alb-5xx-target` | `AWS/ApplicationELB` `HTTPCode_Target_5XX_Count` | LoadBalancer | Sum | 300s | > 10 | 1 of 1 | notBreaching |
| `lb-tbb-service-prod-alb-5xx-elb` | `AWS/ApplicationELB` `HTTPCode_ELB_5XX_Count` | LoadBalancer | Sum | 300s | > 10 | 1 of 1 | notBreaching |
| `prod-tbb-rds-cpu` | `AWS/RDS` `CPUUtilization` | DBInstanceIdentifier `prod-tbb` | Average | 300s | > 80% | 3 of 3 | missing |
| `prod-tbb-rds-freeable-memory` | `AWS/RDS` `FreeableMemory` | DBInstanceIdentifier `prod-tbb` | Average | 300s | < 1,717,986,918 bytes (10% of 16 GiB, `db.t3.xlarge`) | 3 of 3 | missing |
| `prod-tbb-rds-free-storage` | `AWS/RDS` `FreeStorageSpace` | DBInstanceIdentifier `prod-tbb` | Minimum | 300s | < 16,106,127,360 bytes (15% of 100 GiB allocated) | 1 of 1 | missing |
| `prod-tbb-rds-disk-queue-depth` | `AWS/RDS` `DiskQueueDepth` | DBInstanceIdentifier `prod-tbb` | Average | 300s | > 10 | 3 of 3 | missing |
| `tbb-prod-lambda-errors` | `AWS/Lambda` `Errors` | None (all functions in the account and region) | Sum | 300s | > 0 | 1 of 1 | notBreaching |

The thresholds match the Terraform-managed alarms in the other repositories, so
alerting behaves the same across the estate. The `prod-tbb` memory and storage
thresholds are derived from its instance class and allocated storage (PostgreSQL
on standard RDS, no storage autoscaling). If either changes, these thresholds must
be updated by hand.

### Reasoning

- **Consistency with this ADR.** Bringing three resources under Terraform only to
  attach alarms would create the misleading impression of Terraform coverage that
  this ADR set out to avoid, in an account expected to be retired.
- **Low ongoing cost.** One topic and nine alarms are small, and they are retired
  together with the resources they monitor.
- **Recorded, not undocumented.** Recording the exact configuration here, together
  with the commands in the appendix, keeps the exception auditable and reproducible.

### Consequences

- The `tbb-prod-alarms` topic is a dependency of two Terraform roots (`tc-api-prod`,
  `tc-skills-service-prod`) even though it is not in Terraform. It must exist before
  either root is planned.
- Changes to these alarms are made by hand, so there is no code review. Any change
  should be reflected in this addendum.
- The topic is unencrypted, matching the OPC topics. If encryption is added later,
  it must use a customer-managed KMS key whose policy allows
  `cloudwatch.amazonaws.com` (`kms:Decrypt`, `kms:GenerateDataKey*`). CloudWatch
  cannot publish to topics encrypted with the AWS-managed `alias/aws/sns` key.
- The global Lambda alarm also covers any Lambda function added to TBB prod later.
- The `tc-skills-service-prod` root has known drift from the shared module, tracked
  separately. Its TC-1526 alarms were applied with `-target`.
- On migration to OPC prod, create equivalent alarms through the `opc-prod` root
  and delete the manual resources listed above.

### Appendix: commands used

Run with TBB prod credentials (account `968457613372`). In zsh, the tags must be an
array, as below.

```bash
export AWS_REGION=us-east-1
TOPIC=arn:aws:sns:us-east-1:968457613372:tbb-prod-alarms
TAGS=(Key=ManagedBy,Value=manual Key=Ticket,Value=TC-1526)
LB=app/lb-tbb-service/6e11f5079ee133ac
TG=$(aws elbv2 describe-target-groups --names tg-any-ip-HTTP8080 \
  --query 'TargetGroups[0].TargetGroupArn' --output text | sed 's/.*:\(targetgroup\/.*\)/\1/')

# Subscription confirmation (token from the confirmation email; do not click the link)
aws sns confirm-subscription --topic-arn $TOPIC --token <TOKEN> \
  --authenticate-on-unsubscribe true

# lb-tbb-service
aws cloudwatch put-metric-alarm --alarm-name lb-tbb-service-prod-alb-unhealthy-hosts \
  --alarm-description "lb-tbb-service (TBB prod) target group has unhealthy hosts. Manual per ADR 002. TC-1526." \
  --namespace AWS/ApplicationELB --metric-name UnHealthyHostCount \
  --dimensions Name=LoadBalancer,Value=$LB Name=TargetGroup,Value=$TG \
  --statistic Maximum --period 60 --evaluation-periods 5 --datapoints-to-alarm 3 \
  --threshold 0 --comparison-operator GreaterThanThreshold --treat-missing-data notBreaching \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

aws cloudwatch put-metric-alarm --alarm-name lb-tbb-service-prod-alb-latency-p95 \
  --alarm-description "lb-tbb-service (TBB prod) p95 target response time is elevated. Manual per ADR 002. TC-1526." \
  --namespace AWS/ApplicationELB --metric-name TargetResponseTime \
  --dimensions Name=LoadBalancer,Value=$LB \
  --extended-statistic p95 --period 60 --evaluation-periods 5 --datapoints-to-alarm 5 \
  --threshold 2 --comparison-operator GreaterThanThreshold --treat-missing-data notBreaching \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

aws cloudwatch put-metric-alarm --alarm-name lb-tbb-service-prod-alb-5xx-target \
  --alarm-description "lb-tbb-service (TBB prod) targets are returning 5XX responses. Manual per ADR 002. TC-1526." \
  --namespace AWS/ApplicationELB --metric-name HTTPCode_Target_5XX_Count \
  --dimensions Name=LoadBalancer,Value=$LB \
  --statistic Sum --period 300 --evaluation-periods 1 --datapoints-to-alarm 1 \
  --threshold 10 --comparison-operator GreaterThanThreshold --treat-missing-data notBreaching \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

aws cloudwatch put-metric-alarm --alarm-name lb-tbb-service-prod-alb-5xx-elb \
  --alarm-description "lb-tbb-service (TBB prod) is itself returning 5XX responses. Manual per ADR 002. TC-1526." \
  --namespace AWS/ApplicationELB --metric-name HTTPCode_ELB_5XX_Count \
  --dimensions Name=LoadBalancer,Value=$LB \
  --statistic Sum --period 300 --evaluation-periods 1 --datapoints-to-alarm 1 \
  --threshold 10 --comparison-operator GreaterThanThreshold --treat-missing-data notBreaching \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

# prod-tbb (PostgreSQL, db.t3.xlarge, 100 GiB, no storage autoscaling)
aws cloudwatch put-metric-alarm --alarm-name prod-tbb-rds-cpu \
  --alarm-description "RDS prod-tbb (TBB prod) CPU utilization is high. Manual per ADR 002. TC-1526." \
  --namespace AWS/RDS --metric-name CPUUtilization \
  --dimensions Name=DBInstanceIdentifier,Value=prod-tbb \
  --statistic Average --period 300 --evaluation-periods 3 --datapoints-to-alarm 3 \
  --threshold 80 --comparison-operator GreaterThanThreshold --treat-missing-data missing \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

aws cloudwatch put-metric-alarm --alarm-name prod-tbb-rds-freeable-memory \
  --alarm-description "RDS prod-tbb (TBB prod) freeable memory is low (< 10% of 16 GiB, db.t3.xlarge). Manual per ADR 002. TC-1526." \
  --namespace AWS/RDS --metric-name FreeableMemory \
  --dimensions Name=DBInstanceIdentifier,Value=prod-tbb \
  --statistic Average --period 300 --evaluation-periods 3 --datapoints-to-alarm 3 \
  --threshold 1717986918 --comparison-operator LessThanThreshold --treat-missing-data missing \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

aws cloudwatch put-metric-alarm --alarm-name prod-tbb-rds-free-storage \
  --alarm-description "RDS prod-tbb (TBB prod) free storage is low (< 15% of 100 GiB allocated). Manual per ADR 002. TC-1526." \
  --namespace AWS/RDS --metric-name FreeStorageSpace \
  --dimensions Name=DBInstanceIdentifier,Value=prod-tbb \
  --statistic Minimum --period 300 --evaluation-periods 1 --datapoints-to-alarm 1 \
  --threshold 16106127360 --comparison-operator LessThanThreshold --treat-missing-data missing \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

aws cloudwatch put-metric-alarm --alarm-name prod-tbb-rds-disk-queue-depth \
  --alarm-description "RDS prod-tbb (TBB prod) disk queue depth is high. Manual per ADR 002. TC-1526." \
  --namespace AWS/RDS --metric-name DiskQueueDepth \
  --dimensions Name=DBInstanceIdentifier,Value=prod-tbb \
  --statistic Average --period 300 --evaluation-periods 3 --datapoints-to-alarm 3 \
  --threshold 10 --comparison-operator GreaterThanThreshold --treat-missing-data missing \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"

# Global Lambda errors (covers ECSServiceRedeployFunction and any future function)
aws cloudwatch put-metric-alarm --alarm-name tbb-prod-lambda-errors \
  --alarm-description "Any Lambda function in TBB prod (us-east-1) reported errors. Manual per ADR 002. TC-1526." \
  --namespace AWS/Lambda --metric-name Errors \
  --statistic Sum --period 300 --evaluation-periods 1 --datapoints-to-alarm 1 \
  --threshold 0 --comparison-operator GreaterThanThreshold --treat-missing-data notBreaching \
  --alarm-actions $TOPIC --ok-actions $TOPIC --tags "${TAGS[@]}"
```
