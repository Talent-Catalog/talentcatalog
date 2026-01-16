# This file is the main entry to the opc-staging infrastructure
# The idea of using a separate directory is to use different remote state backends and roles
terraform {
  required_version = ">= 1.3.0"
}

# Store the terraform state remotely in S3 bucket
terraform {
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
module "tc-opc-staging" {
  source = "../"

  # ECS
  app = "tc-plus"
  env = "opc-staging"
  site_domain = "plus.tctalent.org"
  container_image = "164804461258.dkr.ecr.eu-west-2.amazonaws.com/app:opc-staging"
  container_port = 8080
  db_enable = true
  db_public_access = false
  db_multi_az =  false
  db_instance_class = "db.t3.medium"
  ecs_tasks_count = 2

  # Spring
  aws_access_key = "<REPLACE_ME>"
  aws_secret_key = "<REPLACE_ME>"

  s3_bucket = "<REPLACE_ME>"

  es_password = "<REPLACE_ME>"
  es_url = "<REPLACE_ME>"
  es_username = "<REPLACE_ME>"
  email_default = "<REPLACE_ME>"
  email_password = "<REPLACE_ME>"
  email_test_override = "<REPLACE_ME>"
  email_user = "<REPLACE_ME>"
  environment = "staging"
  drive_id = "<REPLACE_ME>"
  drive_rootfolder =  "<REPLACE_ME>"
  drive_list_folders_id = "<REPLACE_ME>"
  drive_list_folders_root_id = "<REPLACE_ME>"
  drive_private_key = "<REPLACE_ME>"
  drive_private_key_id = "<REPLACE_ME>"
  gradle_home = "<REPLACE_ME>"
  java_home = "<REPLACE_ME>"
  jwt_secret = "<REPLACE_ME>"
  m2 = "<REPLACE_ME>"
  m2_home = "<REPLACE_ME>"
  server_port = "<REPLACE_ME>"
  server_url = "<REPLACE_ME>"
  sf_base_classic_url = "<REPLACE_ME>"
  sf_base_lightning_url = "<REPLACE_ME>"
  sf_base_login_url = "<REPLACE_ME>"
  sf_consumer_key = "<REPLACE_ME>"
  sf_private_key = "<REPLACE_ME>"
  sf_user = "<REPLACE_ME>"
  slack_token = "<REPLACE_ME>"
  spring_client_url = "<REPLACE_ME>"
  spring_datasource_password = "<REPLACE_ME>"
  spring_datasource_url = "<REPLACE_ME>"
  spring_datasource_username = "<REPLACE_ME>"
  spring_db_pool_max = "<REPLACE_ME>"
  spring_db_pool_min = "<REPLACE_ME>"
  spring_servlet_max_file_size = "<REPLACE_ME>"
  spring_servlet_max_request_size = "<REPLACE_ME>"
  tc_cors_urls = "<REPLACE_ME>"
  tc_db_copy_config = "<REPLACE_ME>"
  translation_password = "<REPLACE_ME>"
  web_admin = "<REPLACE_ME>"
  web_portal = "<REPLACE_ME>"
}
