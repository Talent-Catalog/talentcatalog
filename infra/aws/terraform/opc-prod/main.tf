# This file is the main entry to the opc-prod infrastructure
# The idea of using a separate directory is to use different remote state backends and roles

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
  description = "Slack token (todo: need one for OPC)"
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
  description = "Boot admin password for TC server"
  type        = string
  sensitive   = true
  default     = ""
}

# Configure the AWS provider
# NOTE: Provider configuration MUST remain here (cannot be moved to parent module).
# Providers cannot have configuration parameters injected via module variables.
# Each environment targets a different AWS account via different assume_role ARNs.
# OPC prod account: 289896345557
provider "aws" {
  region = "eu-west-2"

  assume_role {
    role_arn = "arn:aws:iam::289896345557:role/opc-prod-terraform-exec"
  }
}

provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  assume_role {
    role_arn = "arn:aws:iam::289896345557:role/opc-prod-terraform-exec"
  }
}

# Define common tags for all resources
locals {
  common_tags = {
    Project     = "OPC"
    Application = "TC-Server"
    Environment = "prod"
    ManagedBy   = "terraform"
  }
}

# TC-Server infrastructure for OPC AWS production account
module "tc-plus-prod" {
  source = "../"

  providers = {
    aws           = aws
    aws.us_east_1 = aws.us_east_1
  }

  common_tags = local.common_tags

  # ECS configuration
  app             = "tc-server"
  env             = "opc-prod"
  site_domain     = "plus.tctalent.org"
  container_image = "289896345557.dkr.ecr.eu-west-2.amazonaws.com/tc-core:tc-server-prod"
  container_port  = 8080
  ecs_tasks_count = 2
  fargate_cpu     = 1024
  fargate_memory  = 2048

  # Database configuration
  # RDS creates a local database (tcplus), but the service currently connects to the legacy TBB
  # database via spring_datasource_url below. It does not currently connect  to the OPC RDS instance.
  db_enable               = true
  db_public_access        = false
  db_multi_az             = true
  db_instance_class       = "db.m6g.large"
  db_engine_version       = "17.5"
  db_family               = "postgres17"
  db_major_engine_version = "17"
  db_name                 = "tcplus"

  availability_zones = ["eu-west-2a", "eu-west-2b", "eu-west-2c"]

  # Redis cache configuration (todo: enable for production)
  cache_enable          = true
  cache_cluster_id      = "tc-prod-cache"
  cache_node_type       = "cache.t3.micro"
  cache_num_cache_nodes = 3
  cache_engine_version  = "7.1"
  cache_port            = 6379

  # SSM-backed application parameters (stored in SSM, injected into ECS task)
  # Non-secrets: provided directly here
  s3_bucket                             = "files.tbbtalent.org" # todo: confirm bucket name
  translations_bucket                   = "translations.tctalent.org"
  translations_folder                   = "translations"
  environment                           = "opc-prod"
  email_default                         = "-" # todo: confirm if used/needed
  email_test_override                   = "-" # todo: set prod value
  email_user                            = "-" # todo: confirm if used/needed
  email_type                            = "SMTP"
  es_url                                = "https://tc-prod.es.us-east-1.aws.found.io:9243" # todo: retire elasticsearch
  es_username                           = "elastic"                                        # todo: retire elasticsearch
  gradle_home                           = "/usr/local/gradle"
  java_home                             = "/usr/lib/jvm/java"
  logbuilder_include_cpu_utilization    = "true"
  logbuilder_include_memory_utilization = "true"
  m2                                    = "/usr/local/apache-maven/bin"
  m2_home                               = "/usr/local/apache-maven"
  server_port                           = "8080"
  server_url                            = "https://plus.tctalent.org/"
  sf_base_classic_url                   = "https://talentbeyondboundaries.my.salesforce.com/"                                 # todo: either create for OPC or decouple TC+ from SF
  sf_base_lightning_url                 = "https://talentbeyondboundaries.lightning.force.com"                                # todo: either create for OPC or decouple TC+ from SF
  sf_base_login_url                     = "https://login.salesforce.com/"                                                     # todo: either create for OPC or decouple TC+ from SF
  spring_client_url                     = "-"                                                                                 # todo: confirm if used/needed
  spring_datasource_url                 = "jdbc:postgresql://prod-tbb.cskpt7osayvj.us-east-1.rds.amazonaws.com:5432/tctalent" # legacy TBB DB -- remove when cutting over to local RDS
  spring_datasource_username            = "tctalent"
  spring_db_pool_max                    = "50"
  spring_db_pool_min                    = "20"
  spring_servlet_max_file_size          = "10MB"
  spring_servlet_max_request_size       = "10MB"
  tc_api_url                            = "https://api.plus.tctalent.org"               # todo: set TC API URL
  tc_cors_urls                          = "https://tctalent.org"                        # todo: set prod CORS URLs
  tc_db_copy_config                     = "data.sharing/tcCopies.xml"                   # todo: can this be retired?
  tc_destinations                       = "Australia,Canada,New Zealand,United Kingdom" # todo: set TC destinations
  tc_skills_extraction_api_url          = "https://skills.plus.tctalent.org"            # todo: confirm prod URL
  web_admin                             = "https://plus.tctalent.org/admin-portal"
  web_portal                            = "https://plus.tctalent.org/candidate-portal"
  tc_instance_type                      = "TBB"

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

# Configure the opc-prod terraform workspace
# NOTE: The terraform block with backend configuration MUST remain in this file (cannot be moved to parent module).
# This is because backend configuration can only exist in the root module where terraform init/apply is run
terraform {
  required_version = ">= 1.3.0"

  # Store the terraform state remotely in S3 bucket
  backend "s3" {
    bucket         = "opc-shared-terraform-state"
    key            = "prod/talentcatalog/terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "opc-terraform-locks"
    encrypt        = "true"
  }
}
