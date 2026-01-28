# OPC Staging Infrastructure

This directory contains the Terraform configuration for the OPC staging environment.

## Deployment

Terraform can be run immediately without any prerequisites. It will create SSM parameters with 
placeholder values that you'll update manually afterwards.

```bash
cd infra/aws/terraform/opc-staging

# Initialize Terraform
terraform init

# Review the plan
terraform plan

# Apply changes
terraform apply
```

## Post-Deployment Configuration

### Manual SSM Parameter Setup

After running `terraform apply`, you must update SSM parameters with real values. This approach 
keeps secrets out of Terraform state files.

**Security Features:**
1. Terraform protection: Uses `lifecycle.ignore_changes` in parameters.tf so manual updates won't 
be overwritten by subsequent `terraform apply` runs
2. Variable masking: Sensitive variables use `sensitive = true` in variables.tf to hide values in 
Terraform logs and outputs
3. No secrets in state: Only placeholder values are stored in Terraform state; real secrets and 
parameter values are added manually via AWS CLI or in the AWS Console

#### Required SSM Parameters to Update

After running `terraform apply`, update the following parameters with real values:

```bash
# Update AWS Access Key for S3 access
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/AWS_CREDENTIALS_ACCESSKEY" \
  --value "YOUR_ACCESS_KEY_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update AWS Secret Key for S3 access
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/AWS_CREDENTIALS_SECRETKEY" \
  --value "YOUR_SECRET_KEY_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update S3 Bucket Name (e.g., "opc-talentcatalog-staging")
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/AWS_S3_BUCKETNAME" \
  --value "YOUR_S3_BUCKET_NAME_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Duolingo API Secret
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/DUOLINGO_API_APISECRET" \
  --value "YOUR_DUOLINGO_API_SECRET_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite
```

**Note:** Make sure you're authenticated with the correct AWS role that has permissions to update 
SSM parameters in the OPC staging account.

To verify the parameters were updated:

```bash
# Verify Access Key
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/AWS_CREDENTIALS_ACCESSKEY" \
  --region eu-west-2

# Verify Secret Key (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/AWS_CREDENTIALS_SECRETKEY" \
  --with-decryption \
  --region eu-west-2

# Verify S3 Bucket Name
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/AWS_S3_BUCKETNAME" \
  --region eu-west-2

# Verify Duolingo API Secret (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/DUOLINGO_API_APISECRET" \
  --with-decryption \
  --region eu-west-2
```

## Architecture

- **Environment:** OPC Staging
- **Region:** eu-west-2 (London)
- **Application:** TC-Plus
- **Domain:** test.plus.tctalent.org
- **ECS Tasks:** 2 instances
- **Database:** PostgreSQL 17.5 on RDS (db.t3.medium)

## State Management

Terraform state is stored remotely in S3:
- **Bucket:** opc-shared-terraform-state
- **Key:** staging/talentcatalog/terraform.tfstate
- **Region:** eu-west-2
- **Lock Table:** opc-terraform-locks
