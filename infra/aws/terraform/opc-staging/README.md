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

# Update Elasticsearch Password (TODO: retire elasticsearch)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/ELASTICSEARCH_PASSWORD" \
  --value "YOUR_ELASTICSEARCH_PASSWORD_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Elasticsearch URL (TODO: retire elasticsearch)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/ELASTICSEARCH_URL" \
  --value "YOUR_ELASTICSEARCH_URL_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Elasticsearch Username (TODO: retire elasticsearch)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/ELASTICSEARCH_USERNAME" \
  --value "YOUR_ELASTICSEARCH_USERNAME_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Email Default (TODO: confirm if used/needed)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/EMAIL_DEFAULTEMAIL" \
  --value "YOUR_DEFAULT_EMAIL_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Email Password
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/EMAIL_PASSWORD" \
  --value "YOUR_EMAIL_PASSWORD_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Email Test Override (TODO: change to shared address- currently john@)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/EMAIL_TESTOVERRIDEEMAIL" \
  --value "YOUR_TEST_OVERRIDE_EMAIL_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Email User (TODO: confirm if used/needed)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/EMAIL_USER" \
  --value "YOUR_EMAIL_USER_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Environment
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/ENVIRONMENT" \
  --value "opc-staging" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Google Drive Candidate Data Drive ID (TODO: replicate in OPC workspace)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_CANDIDATEDATADRIVEID" \
  --value "YOUR_DRIVE_ID_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Google Drive Candidate Root Folder ID (TODO: replicate in OPC workspace)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_CANDIDATEROOTFOLDERID" \
  --value "YOUR_ROOT_FOLDER_ID_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Google Drive List Folders Drive ID (TODO: replicate in OPC workspace)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_LISTFOLDERSDRIVEID" \
  --value "YOUR_LIST_FOLDERS_DRIVE_ID_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Google Drive List Folders Root ID (TODO: replicate in OPC workspace)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_LISTFOLDERSROOTID" \
  --value "YOUR_LIST_FOLDERS_ROOT_ID_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Google Drive Private Key (TODO: replicate in OPC workspace)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_PRIVATEKEY" \
  --value "YOUR_PRIVATE_KEY_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Google Drive Private Key ID (TODO: replicate in OPC workspace)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_PRIVATEKEYID" \
  --value "YOUR_PRIVATE_KEY_ID_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Gradle Home
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/GRADLE_HOME" \
  --value "/usr/local/gradle" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Java Home
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/JAVA_HOME" \
  --value "/usr/lib/jvm/java" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update JWT Secret
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/JWT_SECRET" \
  --value "YOUR_JWT_SECRET_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update M2
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/M2" \
  --value "/usr/local/apache-maven/bin" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update M2 Home
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/M2_HOME" \
  --value "/usr/local/apache-maven" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Preset API Token
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/PRESET_API_TOKEN" \
  --value "YOUR_PRESET_API_TOKEN_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Preset Secret
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/PRESET_SECRET" \
  --value "YOUR_PRESET_SECRET_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Preset Workspace ID
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/PRESET_WORKSPACE_ID" \
  --value "YOUR_PRESET_WORKSPACE_ID_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Redis Host (TODO: the cache should be created by terraform)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/REDIS_HOST" \
  --value "YOUR_REDIS_HOST_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Redis Port (TODO: the cache should be created by terraform)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/REDIS_PORT" \
  --value "6379" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Server Port
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SERVER_PORT" \
  --value "8080" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Server URL
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SERVER_URL" \
  --value "https://test.plus.tctalent.org/" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Salesforce Base Classic URL (TODO: either create for OPC or decouple TC+ from SF)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SF_BASE_CLASSIC_URL" \
  --value "YOUR_SF_BASE_CLASSIC_URL_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Salesforce Base Lightning URL (TODO: either create for OPC or decouple TC+ from SF)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SF_BASE_LIGHTNING_URL" \
  --value "YOUR_SF_BASE_LIGHTNING_URL_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Salesforce Base Login URL (TODO: either create for OPC or decouple TC+ from SF)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SF_BASE_LOGIN_URL" \
  --value "YOUR_SF_BASE_LOGIN_URL_HERE" \
  --type "String" \
  --region eu-west-2 \
  --overwrite

# Update Salesforce Consumer Key (TODO: either create for OPC or decouple TC+ from SF)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SF_CONSUMER_KEY" \
  --value "YOUR_SF_CONSUMER_KEY_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Salesforce Private Key (TODO: either create for OPC or decouple TC+ from SF)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SF_PRIVATE_KEY" \
  --value "YOUR_SF_PRIVATE_KEY_HERE" \
  --type "SecureString" \
  --region eu-west-2 \
  --overwrite

# Update Salesforce User (TODO: either create for OPC or decouple TC+ from SF)
aws ssm put-parameter \
  --name "/tc-plus/opc-staging/SF_USER" \
  --value "YOUR_SF_USER_HERE" \
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

# Verify Elasticsearch Password (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/ELASTICSEARCH_PASSWORD" \
  --with-decryption \
  --region eu-west-2

# Verify Elasticsearch URL
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/ELASTICSEARCH_URL" \
  --region eu-west-2

# Verify Elasticsearch Username
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/ELASTICSEARCH_USERNAME" \
  --region eu-west-2

# Verify Email Default
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/EMAIL_DEFAULTEMAIL" \
  --region eu-west-2

# Verify Email Password (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/EMAIL_PASSWORD" \
  --with-decryption \
  --region eu-west-2

# Verify Email Test Override
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/EMAIL_TESTOVERRIDEEMAIL" \
  --region eu-west-2

# Verify Email User
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/EMAIL_USER" \
  --region eu-west-2

# Verify Environment
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/ENVIRONMENT" \
  --region eu-west-2

# Verify Google Drive Candidate Data Drive ID (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_CANDIDATEDATADRIVEID" \
  --with-decryption \
  --region eu-west-2

# Verify Google Drive Candidate Root Folder ID (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_CANDIDATEROOTFOLDERID" \
  --with-decryption \
  --region eu-west-2

# Verify Google Drive List Folders Drive ID (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_LISTFOLDERSDRIVEID" \
  --with-decryption \
  --region eu-west-2

# Verify Google Drive List Folders Root ID (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_LISTFOLDERSROOTID" \
  --with-decryption \
  --region eu-west-2

# Verify Google Drive Private Key (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_PRIVATEKEY" \
  --with-decryption \
  --region eu-west-2

# Verify Google Drive Private Key ID (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GOOGLE_DRIVE_PRIVATEKEYID" \
  --with-decryption \
  --region eu-west-2

# Verify Gradle Home
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/GRADLE_HOME" \
  --region eu-west-2

# Verify Java Home
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/JAVA_HOME" \
  --region eu-west-2

# Verify JWT Secret (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/JWT_SECRET" \
  --with-decryption \
  --region eu-west-2

# Verify M2
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/M2" \
  --region eu-west-2

# Verify M2 Home
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/M2_HOME" \
  --region eu-west-2

# Verify Preset API Token (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/PRESET_API_TOKEN" \
  --with-decryption \
  --region eu-west-2

# Verify Preset Secret (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/PRESET_SECRET" \
  --with-decryption \
  --region eu-west-2

# Verify Preset Workspace ID (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/PRESET_WORKSPACE_ID" \
  --with-decryption \
  --region eu-west-2

# Verify Redis Host
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/REDIS_HOST" \
  --region eu-west-2

# Verify Redis Port
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/REDIS_PORT" \
  --region eu-west-2

# Verify Server Port
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SERVER_PORT" \
  --region eu-west-2

# Verify Server URL
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SERVER_URL" \
  --region eu-west-2

# Verify Salesforce Base Classic URL
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SF_BASE_CLASSIC_URL" \
  --region eu-west-2

# Verify Salesforce Base Lightning URL
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SF_BASE_LIGHTNING_URL" \
  --region eu-west-2

# Verify Salesforce Base Login URL
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SF_BASE_LOGIN_URL" \
  --region eu-west-2

# Verify Salesforce Consumer Key (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SF_CONSUMER_KEY" \
  --with-decryption \
  --region eu-west-2

# Verify Salesforce Private Key (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SF_PRIVATE_KEY" \
  --with-decryption \
  --region eu-west-2

# Verify Salesforce User (with decryption)
aws ssm get-parameter \
  --name "/tc-plus/opc-staging/SF_USER" \
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
