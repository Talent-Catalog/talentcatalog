# GRN Staging Environment (OPC AWS Account)

Deploy the TC-Server infrastructure for **GRN (Global Refugee Network)** to the OPC AWS staging account (`164804461258`, `eu-west-2`). 
This runs **alongside** the existing opc-staging deployment in the same account, with separate state, resources, and domain.

- **Domain:** [test.globalrefugee.net](https://test.globalrefugee.net)
- **Resource prefix:** `grn-staging` (VPC, ECS cluster, ALB, RDS, etc.)
- **SSM parameter path:** `/grn/staging/`
- **ECR repository:** `grn-core` (image tag e.g. `grn-staging`)

## Prerequisites

- AWS CLI configured with credentials that can assume `arn:aws:iam::164804461258:role/opc-staging-terraform-exec`
- Terraform >= 1.3
- The S3 backend bucket (`opc-shared-terraform-state`) and DynamoDB lock table (`opc-terraform-locks`) 
  must already exist in the OPC account

## 1. Initialise Terraform

```bash
cd infra/aws/terraform/grn-staging
terraform init
```

## 2. Set secrets

Create/edit `secrets.auto.tfvars` in this directory with the real secret values (same structure as 
opc-staging). This file is git-ignored and auto-loaded by Terraform. **Do not commit it.**

Include at least:

- `aws_access_key`, `aws_secret_key`
- `jwt_secret`, `spring_datasource_password`
- `tc_api_key`, `translation_password`, `tc_boot_admin_password`
- Other secrets as required by the application (see opc-staging README for the full list)

## 3. Deploy infrastructure

```bash
terraform plan -out tfplan
terraform apply tfplan
```

This creates a full stack for GRN staging (VPC, RDS, ECS, ALB, Route53, ACM, ElastiCache, ECR repo `grn-core`) 
and populates SSM under `/grn/staging/`.

## 4. DNS

Point the domain **globalrefugee.net** (and www if desired) at the ALB output (e.g. via Route53). 
Terraform will create the ACM certificate and ALB; you need to create the DNS records that point to 
the ALB.

## 5. Build and push container image

Build and push the app image to the GRN ECR repo with tag `grn-staging`:

```bash
# From project root, after AWS auth and ECR login
# Build and push to 164804461258.dkr.ecr.eu-west-2.amazonaws.com/grn-core:grn-staging
```

Use CI or the same pattern as opc-staging (e.g. a Gradle property or env for `grn-staging` that sets 
the image target).

## State and coexistence with opc-staging

- **State key:** `staging/grn/terraform.tfstate` (opc-staging uses `staging/talentcatalog/terraform.tfstate`).
- **Same account, same role:** Both use the OPC staging account and `opc-staging-terraform-exec` role.
- **Separate resources:** All resources are prefixed `grn-staging`; no overlap with opc-staging.
