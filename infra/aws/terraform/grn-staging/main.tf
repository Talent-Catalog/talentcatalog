# This file is the main entry for GRN staging infrastructure in the OPC AWS staging account.
# It runs alongside opc-staging in the same account (164804461258) with separate state and resources.

# Secrets loaded from secrets.auto.tfvars and passed through to the child module
variable "aws_access_key" {
  description = "AWS access key for S3 access"
  type        = string
  sensitive   = true
  default     = ""
}

variable "aws_secret_key" {
  description = "AWS secret key for S3 access"
  type        = string
  sensitive   = true
  default     = ""
}

variable "duolingo_api_secret" {
  description = "Duolingo API secret"
  type        = string
  sensitive   = true
  default     = ""
}

variable "es_password" {
  description = "Elasticsearch password (todo: retire elasticsearch)"
  type        = string
  sensitive   = true
  default     = ""
}

variable "email_password" {
  description = "Email password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "drive_id" {
  description = "Google Drive candidate data drive ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "drive_rootfolder" {
  description = "Google Drive candidate root folder ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "drive_list_folders_id" {
  description = "Google Drive list folders drive ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "drive_list_folders_root_id" {
  description = "Google Drive list folders root ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "drive_private_key" {
  description = "Google Drive private key (PEM format with literal \\n)"
  type        = string
  sensitive   = true
  default     = ""
}

variable "drive_private_key_id" {
  description = "Google Drive private key ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "jwt_secret" {
  description = "JWT secret"
  type        = string
  sensitive   = true
  default     = ""
}

variable "preset_api_token" {
  description = "Preset API token"
  type        = string
  sensitive   = true
  default     = ""
}

variable "preset_secret" {
  description = "Preset secret"
  type        = string
  sensitive   = true
  default     = ""
}

variable "preset_workspace_id" {
  description = "Preset workspace ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "sf_consumer_key" {
  description = "Salesforce connected app consumer key"
  type        = string
  sensitive   = true
  default     = ""
}

variable "sf_private_key" {
  description = "Salesforce private key"
  type        = string
  sensitive   = true
  default     = ""
}

variable "sf_user" {
  description = "Salesforce user for access token"
  type        = string
  sensitive   = true
  default     = ""
}

variable "slack_channel_id" {
  description = "Slack channel ID for job posts (test: C048GS1KHPG, live: C029WMY6H1U)"
  type        = string
}

variable "slack_token" {
  description = "Slack token"
  type        = string
  sensitive   = true
  default     = ""
}

variable "spring_datasource_password" {
  description = "Spring datasource (RDS) password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "tc_api_key" {
  description = "TC API key"
  type        = string
  sensitive   = true
  default     = ""
}

variable "translation_password" {
  description = "Translation password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "tc_boot_admin_password" {
  description = "System admin password for TC"
  type        = string
  sensitive   = true
  default     = ""
}

# Same OPC staging account as opc-staging
provider "aws" {
  region = "eu-west-2"

  assume_role {
    role_arn = "arn:aws:iam::164804461258:role/opc-staging-terraform-exec"
  }
}

provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  assume_role {
    role_arn = "arn:aws:iam::164804461258:role/opc-staging-terraform-exec"
  }
}

locals {
  common_tags = {
    Project     = "GRN"
    Application = "TC-Server"
    Environment = "staging"
    ManagedBy   = "terraform"
  }
}

# GRN staging: parallel deployment in OPC staging account, domain test.globalrefugee.net
module "grn_staging" {
  source = "../"

  providers = {
    aws           = aws
    aws.us_east_1 = aws.us_east_1
  }

  common_tags = local.common_tags

  # ECS configuration (separate ECR repo in same account to avoid name conflict with opc-staging)
  app                 = "grn"
  env                 = "staging"
  site_domain         = "test.globalrefugee.net"
  ecr_repository_name = "grn-core"
  container_image     = "164804461258.dkr.ecr.eu-west-2.amazonaws.com/grn-core:grn-staging"
  container_port      = 8080
  ecs_tasks_count     = 1
  fargate_cpu         = 512
  fargate_memory      = 2048

  # Database configuration
  db_enable               = true
  db_public_access        = false
  db_multi_az             = false
  db_instance_class       = "db.t3.medium"
  db_engine_version       = "17.5"
  db_family               = "postgres17"
  db_major_engine_version = "17"
  db_name                 = "tcplus"

  availability_zones = ["eu-west-2a", "eu-west-2b", "eu-west-2c"]

  # Redis cache (unique cluster id in same account)
  cache_enable          = true
  cache_cluster_id      = "grn-staging-cache"
  cache_node_type       = "cache.t3.micro"
  cache_num_cache_nodes = 3
  cache_engine_version  = "7.1"
  cache_port            = 6379

  # SSM-backed application parameters
  cloudfront_enable                     = true
  candidate_files_bucket                = "candidate-files.test.globalrefugee.net"
  s3_bucket                             = "files.tbbtalent.org" # todo: confirm or set GRN bucket
  translations_bucket                   = "translations.test.globalrefugee.net"
  translations_folder                   = "translations"
  environment                           = "grn-staging"
  email_default                         = "-"
  email_test_override                   = "-"
  email_user                            = "-"
  email_type                            = "SMTP"
  es_url                                = "https://tc-staging.es.us-east-1.aws.found.io:9243" # todo: retire or set GRN
  es_username                           = "elastic"
  gradle_home                           = "/usr/local/gradle"
  java_home                             = "/usr/lib/jvm/java"
  logbuilder_include_cpu_utilization    = "true"
  logbuilder_include_memory_utilization = "true"
  m2                                    = "/usr/local/apache-maven/bin"
  m2_home                               = "/usr/local/apache-maven"
  server_port                           = "8080"
  server_url                            = "https://test.globalrefugee.net/"
  sf_base_classic_url                   = "https://talentbeyondboundaries--sfstaging.sandbox.my.salesforce.com/" # todo: set GRN if different
  sf_base_lightning_url                 = "https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com"
  sf_base_login_url                     = "https://test.salesforce.com/"
  spring_client_url                     = "-"
  # Empty so SPRING_DATASOURCE_URL is auto-populated from the RDS created by this stack.
  # The provided spring_datasource_username/password are used to create the RDS master user and are written to SSM for the app.
  spring_datasource_url           = "" # use RDS created by this stack
  spring_datasource_username      = "tctalent"
  spring_db_pool_max              = "50"
  spring_db_pool_min              = "20"
  spring_servlet_max_file_size    = "10MB"
  spring_servlet_max_request_size = "10MB"
  tc_api_url                      = "https://test.api.globalrefugee.net"
  tc_cors_urls                    = "https://test.globalrefugee.net,https://www.test.globalrefugee.net"
  tc_db_copy_config               = "data.sharing/tcCopies.xml"
  tc_destinations                 = "Australia,Canada,New Zealand,United Kingdom"
  tc_skills_extraction_api_url    = "https://test.skills.globalrefugee.net"
  web_admin                       = "https://test.globalrefugee.net/admin-portal"
  web_portal                      = "https://test.globalrefugee.net/candidate-portal"
  tc_instance_type                = "GRN"

  # Secrets: loaded from secrets.auto.tfvars
  aws_access_key             = var.aws_access_key
  aws_secret_key             = var.aws_secret_key
  duolingo_api_secret        = var.duolingo_api_secret
  es_password                = var.es_password
  email_password             = var.email_password
  drive_id                   = var.drive_id
  drive_rootfolder           = var.drive_rootfolder
  drive_list_folders_id      = var.drive_list_folders_id
  drive_list_folders_root_id = var.drive_list_folders_root_id
  drive_private_key          = var.drive_private_key
  drive_private_key_id       = var.drive_private_key_id
  jwt_secret                 = var.jwt_secret
  preset_api_token           = var.preset_api_token
  preset_secret              = var.preset_secret
  preset_workspace_id        = var.preset_workspace_id
  sf_consumer_key            = var.sf_consumer_key
  sf_private_key             = var.sf_private_key
  sf_user                    = var.sf_user
  slack_channel_id           = var.slack_channel_id
  slack_token                = var.slack_token
  spring_datasource_password = var.spring_datasource_password
  tc_api_key                 = var.tc_api_key
  translation_password       = var.translation_password
  tc_boot_admin_password     = var.tc_boot_admin_password
}

terraform {
  required_version = ">= 1.3.0"

  backend "s3" {
    bucket         = "opc-shared-terraform-state"
    key            = "staging/grn/terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "opc-terraform-locks"
    encrypt        = "true"
  }
}
