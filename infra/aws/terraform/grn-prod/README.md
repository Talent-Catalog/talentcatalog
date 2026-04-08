# GRN Production Environment (OPC AWS Account)

Deploy the TC-Server infrastructure for **GRN (Global Refugee Network)** to the OPC AWS prod account (`289896345557`, `eu-west-2`).
This runs **alongside** the existing opc-prod deployment in the same account, with separate state, resources, and domain.

- **Domain:** [globalrefugee.net](https://globalrefugee.net)
- **Resource prefix:** `grn-prod` (VPC, ECS cluster, ALB, RDS, etc.)
- **SSM parameter path:** `/grn/prod/`
- **ECR repository:** `grn-core` (image tag e.g. `grn-prod`)

## Architecture

Traffic flows through CloudFront, which routes requests to either the ALB (application) or S3 (candidate files):

```
Internet -> Route53 (globalrefugee.net)
              -> CloudFront distribution
                   /o/*      -> S3 bucket (candidate-files.globalrefugee.net, eu-west-2)
                   /* (default) -> ALB (HTTP) -> ECS Fargate (Spring Boot)
```

- **CloudFront** is the public entry point for all traffic (global service)
- **S3 candidate files bucket:** `candidate-files.globalrefugee.net` (eu-west-2)
- **ALB** is an origin behind CloudFront (CloudFront connects via HTTP)
- **ACM certificate for CloudFront** is issued in `us-east-1` (AWS requirement); all other resources remain in `eu-west-2`

## Prerequisites

- AWS CLI configured with credentials that can assume `arn:aws:iam::289896345557:role/opc-prod-terraform-exec`
- Terraform >= 1.3
- The S3 backend bucket (`opc-shared-terraform-state`) and DynamoDB lock table (`opc-terraform-locks`) 
  must already exist in the OPC account
- Route53 hosted zone for `globalrefugee.net` must already exist

## 1. Initialise Terraform

```bash
cd infra/aws/terraform/grn-prod
terraform init
```

## 2. Set secrets

Create/edit `secrets.auto.tfvars` in this directory with the real secret values (same structure as
grn-staging). This file is git-ignored and auto-loaded by Terraform. **Do not commit it.**

Include at least:

- `aws_access_key`, `aws_secret_key`
- `jwt_secret`, `spring_datasource_password`
- `tc_api_key`, `translation_password`, `tc_boot_admin_password`
- Other secrets as required by the application (see opc-prod README for the full list)

## 3. Deploy infrastructure

```bash
terraform plan -out tfplan
terraform apply tfplan
```

This creates the full GRN production stack:

- **Networking:** VPC, subnets, security groups
- **Compute:** ECS Fargate cluster and service (2 tasks), ALB
- **Database:** RDS PostgreSQL (db.m6g.large, multi-AZ), ElastiCache Redis
- **Storage:** S3 bucket for candidate file attachments (`candidate-files.globalrefugee.net`)
- **CDN:** CloudFront distribution with two origins (ALB + S3)
- **TLS:** ACM certificates (eu-west-2 for ALB, us-east-1 for CloudFront)
- **DNS:** Route53 A record pointing to CloudFront
- **Config:** SSM parameters under `/grn/prod/`, ECR repository `grn-core`

Note: CloudFront distribution creation can take 5-15 minutes. ACM certificate validation requires
the Route53 hosted zone for `globalrefugee.net` to already exist.

## 4. Verify deployment

After `terraform apply` completes, verify CloudFront is serving traffic:

```bash
curl -I https://globalrefugee.net/
```

Look for `server: CloudFront` and `x-cache` headers in the response.

## 5. Build and push container image

Build and push the app image to the GRN ECR repo with tag `grn-prod`:

```bash
# From project root, after AWS auth and ECR login
# Build and push to 289896345557.dkr.ecr.eu-west-2.amazonaws.com/grn-core:grn-prod
```

Use CI or the same pattern as opc-prod (e.g. a Gradle property or env for `grn-prod` that sets
the image target).

## CloudFront and S3 configuration

This environment has `cloudfront_enable = true`, which creates:

| Resource | Region | Purpose |
|----------|--------|---------|
| CloudFront distribution | Global | Public entry point, routes `/o/*` to S3 and everything else to ALB |
| S3 bucket (`candidate-files.globalrefugee.net`) | eu-west-2 | Stores candidate file attachments with opaque UUID keys |
| CloudFront Origin Access Control (OAC) | Global | Grants CloudFront read access to the S3 bucket |
| ACM certificate (CloudFront) | us-east-1 | TLS cert for CloudFront (AWS requires us-east-1) |

The S3 bucket is fully private (public access blocked). CloudFront reads files via OAC. The application
writes files to S3 using static AWS credentials injected via SSM environment variables.

The bucket name is available to the application as the `AWS_S3_CANDIDATE_FILES_BUCKET` environment variable.

## State and coexistence with opc-prod

- **State key:** `prod/grn/terraform.tfstate` (opc-prod uses `prod/talentcatalog/terraform.tfstate`).
- **Same account, same role:** Both use the OPC prod account and `opc-prod-terraform-exec` role.
- **Separate resources:** All resources are prefixed `grn-prod`; no overlap with opc-prod.
