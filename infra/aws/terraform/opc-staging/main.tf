# This file is the main entry to the opc-staging infrastructure
# The idea of using a separate directory is to use different remote state backends and roles

# Configure  the opc-staging terraform workspace
terraform {
  required_version = ">= 1.3.0"

  # Store the terraform state remotely in S3 bucket
  backend "s3" {
    bucket         = "opc-shared-terraform-state"
    key            = "staging/talentcatalog/terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "opc-terraform-locks"
    encrypt        = "true"
  }
}

# Configure the AWS provider
provider "aws" {
  region = "eu-west-2"

  assume_role {
    role_arn = "arn:aws:iam::164804461258:role/opc-staging-terraform-exec"
  }
}

# Define common tags for all resources
locals {
  common_tags = {
    Project     = "OPC"
    Application = "TC-Plus"
    Environment = "staging"
    ManagedBy   = "terraform"
  }
}

# Include the shared configuration
module "tc-plus-staging" {
  source = "../"

  common_tags = local.common_tags

  # ECS configuration
  app = "tc-plus"
  env = "opc-staging"
  site_domain = "test.plus.tctalent.org"
  container_image = "164804461258.dkr.ecr.eu-west-2.amazonaws.com/app:tc-plus-staging"
  container_port = 8080
  db_enable = true
  db_public_access = false
  db_multi_az =  false
  db_instance_class = "db.t3.medium"
  db_engine_version = "17.5"
  db_family = "postgres17"
  db_major_engine_version = "17"
  db_name = "tcplus"
  ecs_tasks_count = 2
  availability_zones = ["eu-west-2a", "eu-west-2b", "eu-west-2c"]

  # Spring application configuration

  # todo -- SM: The below service configuration will be addressed in a follow-up PR / PRs

  # AWS S3 configuration: Terraform creates SSM parameters with placeholder values.
  # Update manually after terraform apply (see README.md for AWS CLI commands):
  # - /tc-plus/opc-staging/AWS_CREDENTIALS_ACCESSKEY (String)
  # - /tc-plus/opc-staging/AWS_CREDENTIALS_SECRETKEY (SecureString)
  # - /tc-plus/opc-staging/AWS_S3_BUCKETNAME (String)
  # - /tc-plus/opc-staging/DUOLINGO_API_APISECRET (SecureString)
  # - /tc-plus/opc-staging/ELASTICSEARCH_PASSWORD (SecureString) - todo: retire elasticsearch
  # - /tc-plus/opc-staging/ELASTICSEARCH_URL (String) - todo: retire elasticsearch
  # - /tc-plus/opc-staging/ELASTICSEARCH_USERNAME (String) - todo: retire elasticsearch
  # - /tc-plus/opc-staging/EMAIL_DEFAULTEMAIL (String) - todo: confirm if used/needed
  # - /tc-plus/opc-staging/EMAIL_PASSWORD (SecureString)
  # - /tc-plus/opc-staging/EMAIL_TESTOVERRIDEEMAIL (String) - todo: change to shared address
  # - /tc-plus/opc-staging/EMAIL_USER (String) - todo: confirm if used/needed
  # - /tc-plus/opc-staging/ENVIRONMENT (String)
  # - /tc-plus/opc-staging/GOOGLE_DRIVE_CANDIDATEDATADRIVEID (SecureString) - todo: replicate in OPC workspace
  # - /tc-plus/opc-staging/GOOGLE_DRIVE_CANDIDATEROOTFOLDERID (SecureString) - todo: replicate in OPC workspace
  # - /tc-plus/opc-staging/GOOGLE_DRIVE_LISTFOLDERSDRIVEID (SecureString) - todo: replicate in OPC workspace
  # - /tc-plus/opc-staging/GOOGLE_DRIVE_LISTFOLDERSROOTID (SecureString) - todo: replicate in OPC workspace
  # - /tc-plus/opc-staging/GOOGLE_DRIVE_PRIVATEKEY (SecureString) - todo: replicate in OPC workspace
  # - /tc-plus/opc-staging/GOOGLE_DRIVE_PRIVATEKEYID (SecureString) - todo: replicate in OPC workspace
  # - /tc-plus/opc-staging/GRADLE_HOME (String)
  # - /tc-plus/opc-staging/JAVA_HOME (String)
  # - /tc-plus/opc-staging/JWT_SECRET (SecureString)
  # - /tc-plus/opc-staging/M2 (String)
  # - /tc-plus/opc-staging/M2_HOME (String)
  # - /tc-plus/opc-staging/PRESET_API_TOKEN (SecureString)
  # - /tc-plus/opc-staging/PRESET_SECRET (SecureString)
  # - /tc-plus/opc-staging/PRESET_WORKSPACE_ID (SecureString)
  # - /tc-plus/opc-staging/REDIS_HOST (String) - todo: the cache should be created by terraform
  # - /tc-plus/opc-staging/REDIS_PORT (String) - todo: the cache should be created by terraform
  # - /tc-plus/opc-staging/SERVER_PORT (String)
  # - /tc-plus/opc-staging/SERVER_URL (String)
  # - /tc-plus/opc-staging/SF_BASE_CLASSIC_URL (String) - todo: either create for OPC or decouple TC+ from SF
  # - /tc-plus/opc-staging/SF_BASE_LIGHTNING_URL (String) - todo: either create for OPC or decouple TC+ from SF
  # - /tc-plus/opc-staging/SF_BASE_LOGIN_URL (String) - todo: either create for OPC or decouple TC+ from SF
  # - /tc-plus/opc-staging/SF_CONSUMER_KEY (SecureString) - todo: either create for OPC or decouple TC+ from SF
  # - /tc-plus/opc-staging/SF_PRIVATE_KEY (SecureString) - todo: either create for OPC or decouple TC+ from SF
  # - /tc-plus/opc-staging/SF_USER (SecureString) - todo: either create for OPC or decouple TC+ from SF
  # - /tc-plus/opc-staging/SLACK_TOKEN (SecureString) - todo: need one for OPC
  # - /tc-plus/opc-staging/SPRING_BOOT_ADMIN_CLIENT_URL (String) - todo: confirm if used/needed
  # - /tc-plus/opc-staging/SPRING_DATASOURCE_PASSWORD (SecureString)
  # - /tc-plus/opc-staging/SPRING_DATASOURCE_URL (String) - uses RDS endpoint when db_enable=true
  # - /tc-plus/opc-staging/SPRING_DATASOURCE_USERNAME (String)
  # - /tc-plus/opc-staging/SPRING_DBPOOL_MAX (String)
  # - /tc-plus/opc-staging/SPRING_DBPOOL_MIN (String)
  # - /tc-plus/opc-staging/SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE (String)
  # - /tc-plus/opc-staging/SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE (String)
  # - /tc-plus/opc-staging/TC_API_KEY (SecureString)
  # - /tc-plus/opc-staging/TC_API_URL (String)
  # - /tc-plus/opc-staging/TC_CORS_URLS (String) - todo: amplify urls
  # - /tc-plus/opc-staging/TC_DESTINATIONS (String)
  # - /tc-plus/opc-staging/TC_PARTNER_DBCOPY_CONFIG (String) - todo: can this be retired?
  # Note: lifecycle.ignore_changes prevents Terraform from overwriting manual updates

  # todo -- tc_skills_extraction_api_url : https://skills.plus.tctalent.org
  translation_password = "<REPLACE_ME>" # can't be hardcoded for repository -- find another way todo

  web_admin = "https://test.plus.tctalent.org/admin-portal"
  web_portal = "https://test.plus.tctalent.org/candidate-portal"

}
