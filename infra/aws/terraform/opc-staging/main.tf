# This file is the main entry to the opc-staging infrastructure
# The idea of using a separate directory is to use different remote state backends and roles
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

provider "aws" {
  region = "eu-west-2"

  assume_role {
    role_arn = "arn:aws:iam::164804461258:role/opc-staging-terraform-exec"
  }
}

# Include the shared configuration
module "tc-plus-staging" {
  source = "../"

  # ECS
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

  # Spring
  # todo create s3 access and required buckets
  aws_access_key = "<REPLACE_ME>" # for s3 -- shouldn't be hardcoded todo
  aws_secret_key = "<REPLACE_ME>" # for s3 -- shouldn't be hardcoded todo
  s3_bucket = "<REPLACE_ME>" # for s3 -- switch to OPC todo

  # todo missing duolingo api secret -- shouldn't be hardcoded

  # todo retire elasticsearch
  es_password = "<REPLACE_ME>" # shouldn't be hardcoded -- remove elasticsearch todo
  es_url = "<REPLACE_ME>" # -- remove elasticsearch todo
  es_username = "<REPLACE_ME>" # -- remove elasticsearch todo

  email_default = "<REPLACE_ME>" # todo confirm if this is used / needed - it is not configured in the AWS staging task definition
  email_password = "<REPLACE_ME>" # shouldn't be hardcoded todo
  email_test_override = "john@cameronfoundation.org" # todo change this to a shared address

  email_user = "<REPLACE_ME>" # todo confirm if this is used / needed - it is not configured in the AWS staging task definition

  environment = "plus-staging"

  # todo replicate in OPC workspace
  drive_id = "<REPLACE_ME>"
  drive_rootfolder =  "<REPLACE_ME>"
  drive_list_folders_id = "<REPLACE_ME>"
  drive_list_folders_root_id = "<REPLACE_ME>"
  drive_private_key = "<REPLACE_ME>"
  drive_private_key_id = "<REPLACE_ME>"

  gradle_home = "/usr/local/gradle"
  java_home = "/usr/lib/jvm/java"
  jwt_secret = "<REPLACE_ME>" # todo do not hardcode
  m2 = "/usr/local/apache-maven/bin"
  m2_home = "/usr/local/apache-maven"

  # todo preset_api_token, preset_secret (do not hardcode), preset_workspace_id
  # todo redis_host, redis_port

  server_port = "8080"
  server_url = "https://plus.test.tctalent.org/"

  # todo either create for OPC -- or decouple TC+ from SF ?
  sf_base_classic_url = "<REPLACE_ME>"
  sf_base_lightning_url = "<REPLACE_ME>"
  sf_base_login_url = "<REPLACE_ME>"
  sf_consumer_key = "<REPLACE_ME>"
  sf_private_key = "<REPLACE_ME>"
  sf_user = "<REPLACE_ME>"

  slack_token = "<REPLACE_ME>" # todo need one for OPC but in any case shouldn't be hardcoded
  spring_client_url = "-" # todo confirm if this is used or needed, there is no value configured for it in the stating service task definition
  spring_datasource_password = "<REPLACE_ME>" # shouldn't be hardcoded -- todo
  spring_datasource_url = "jdbc:postgresql://tbbtalent-prod.cy7icd7y1lyr.us-east-1.rds.amazonaws.com:5432/tctalent" # todo after RDS DB is created
  spring_datasource_username = "tctalent"
  spring_db_pool_max = "50"
  spring_db_pool_min = "20"
  spring_servlet_max_file_size = "10MB"
  spring_servlet_max_request_size = "10MB"

  # todo tc_api_key, tc_api_url

  tc_cors_urls = "https://plus.test.tctalent.org,https://*.d2jx6ziu0w8kq9.amplifyapp.com,https://*.d1bt868vpd541m.amplifyapp.com" # todo amplify urls

  #  todo tc_destinations

  tc_db_copy_config = "data.sharing/tcCopies.xml" # todo -- can this be retired?

  # todo -- tc_skills_extraction_api_url : https://skills.plus.tctalent.org
  translation_password = "<REPLACE_ME>" # can't be hardcoded for repository -- find another way todo

  web_admin = "https://test.plus.tctalent.org/admin-portal"
  web_portal = "https://test.plus.tctalent.org/candidate-portal"

  acm_certificate_tags = {
    Project     = "OPC"
    Application = "TC-Plus"
    Environment = "staging"
    Component   = "acm"
    Purpose     = "tls"
    ManagedBy   = "terraform"
  }

  alb_tags = {
    Project     = "OPC"
    Application = "TC-Plus"
    Environment = "staging"
    Component   = "alb"
    Purpose     = "public-ingress"
    ManagedBy   = "terraform"
  }
}
