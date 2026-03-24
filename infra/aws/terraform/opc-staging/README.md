# TC-Server OPC Staging Environment

Deploy the TC-Server infrastructure to the OPC AWS staging account (`164804461258`, `eu-west-2`).

## Prerequisites

- AWS CLI configured with credentials that can assume `arn:aws:iam::164804461258:role/opc-staging-terraform-exec`
- Terraform >= 1.3
- The S3 backend bucket (`opc-shared-terraform-state`) and DynamoDB lock table (`opc-terraform-locks`)
  must already exist in the OPC account

## 1. Initialise Terraform

```bash
cd infra/aws/terraform/opc-staging
terraform init
```

## 2. Set secrets

Create/edit `secrets.auto.tfvars` in this directory with the real secret values:

```hcl
aws_access_key             = "..."
aws_secret_key             = "..."
duolingo_api_secret        = "..."
es_password                = "..."  # todo: retire elasticsearch
email_password             = "..."
drive_id                   = "..."
drive_rootfolder           = "..."
drive_list_folders_id      = "..."
drive_list_folders_root_id = "..."
drive_private_key          = "-----BEGIN PRIVATE KEY-----\\nMIIEv...\\n-----END PRIVATE KEY-----\\n"
drive_private_key_id       = "..."
jwt_secret                 = "..."
preset_api_token           = "..."
preset_secret              = "..."
preset_workspace_id        = "..."
sf_consumer_key            = "..."  # todo: either create for OPC or decouple TC+ from SF
sf_private_key             = "..."  # todo: either create for OPC or decouple TC+ from SF
sf_user                    = "..."  # todo: either create for OPC or decouple TC+ from SF
slack_token                = "..."  # todo: need one for OPC
spring_datasource_password = "..."
tc_api_key                 = "..."
translation_password       = "..."
```

This file is git-ignored and auto-loaded by Terraform. **Do not commit it.**

> **Note on `drive_private_key`:** The PEM private key must be stored as a single line with literal
> `\n` characters (e.g., `-----BEGIN PRIVATE KEY-----\nMIIEvg...\n-----END PRIVATE KEY-----\n`).
> The Java application replaces these literal `\n` sequences with actual newlines at runtime.

## 3. Deploy infrastructure

```bash
terraform plan -out tfplan
terraform apply tfplan
```

This creates all infrastructure (VPC, RDS, ECS, ALB, Route53, ACM, ElastiCache, ECR) and populates
all SSM parameters with real values:

- **Secrets** (`aws_secret_key`, `jwt_secret`, `spring_datasource_password`, etc.) are set from
  `secrets.auto.tfvars` values and stored as `SecureString` SSM parameters
- **Non-secrets** (`environment`, `server_port`, `gradle_home`, etc.) are set directly from
  `main.tf` values and stored as `String` SSM parameters
- **Auto-populated** parameters are computed by Terraform from infrastructure resources:
  - `SPRING_DATASOURCE_URL` is built from the RDS endpoint (when `db_enable=true`)
  - `REDIS_HOST` and `REDIS_PORT` are set from the ElastiCache cluster (when `cache_enable=true`)

## Secret and parameter updates

To update any of the secrets or parameters, simply update the relevant SSM parameter directly in the
AWS console.

Then restart the ECS service to pick up the new values.

ECS tasks that restart (scaling, crashes, deployments) automatically fetch the current SSM values
without needing to re-run Terraform.
