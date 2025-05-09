# This file is the main entry to the test infrastructure
# It is a copy of prod env, the idea of using a separate directory is to use different remote state backends,
# and isolate test, and prod environments totally, also to be able to do customization based on environment
# it can be changed later to use workspaces instead of directories to follow the DRY concept!
terraform {
  required_version = ">= 1.3.0"
}

# Store the terraform state remotely in S3 bucket
terraform {
  backend "s3" {
    bucket = "tbbtalent-terraform-state-test"
    key    = "terraform.tfstate"
    region = "us-east-1"
    # todo: add dynamodb for state lock
  }
}

provider "aws" {
  region = "us-east-1"
}

# Include the flat configuration
module "website" {
  source = "../"

  app                             = var.app
  env                             = var.env
  site_domain                     = var.site_domain
  container_image                 = var.container_image
  container_port                  = var.container_port
  db_public_access                = var.db_public_access
  ecs_tasks_count                 = var.ecs_tasks_count
  db_multi_az                     = var.db_multi_az
  db_instance_class               = var.db_instance_class
  aws_access_key                  = var.aws_access_key
  aws_secret_key                  = var.aws_secret_key
  s3_bucket                       = var.s3_bucket
  es_password                     = var.es_password
  es_url                          = var.es_url
  es_username                     = var.es_username
  email_default                   = var.email_default
  email_password                  = var.email_password
  email_test_override             = var.email_test_override
  email_user                      = var.email_user
  environment                     = var.environment
  drive_id                        = var.drive_id
  drive_rootfolder                = var.drive_rootfolder
  drive_list_folders_id           = var.drive_list_folders_id
  drive_list_folders_root_id      = var.drive_list_folders_root_id
  drive_private_key               = var.drive_private_key
  drive_private_key_id            = var.drive_private_key_id
  gradle_home                     = var.gradle_home
  java_home                       = var.java_home
  jwt_secret                      = var.jwt_secret
  m2                              = var.m2
  m2_home                         = var.m2_home
  server_port                     = var.server_port
  server_url                      = var.server_url
  sf_base_classic_url             = var.sf_base_classic_url
  sf_base_lightning_url           = var.sf_base_lightning_url
  sf_base_login_url               = var.sf_base_login_url
  sf_consumer_key                 = var.sf_consumer_key
  sf_private_key                  = var.sf_private_key
  sf_user                         = var.sf_user
  slack_token                     = var.slack_token
  spring_client_url               = var.spring_client_url
  db_enable                       = var.db_enable
  spring_datasource_password      = var.spring_datasource_password
  spring_datasource_url           = var.spring_datasource_url
  spring_datasource_username      = var.spring_datasource_username
  spring_db_pool_max              = var.spring_db_pool_max
  spring_db_pool_min              = var.spring_db_pool_min
  spring_servlet_max_file_size    = var.spring_servlet_max_file_size
  spring_servlet_max_request_size = var.spring_servlet_max_request_size
  tbb_cors_urls                   = var.tbb_cors_urls
  tbb_db_copy_config              = var.tbb_db_copy_config
  translation_password            = var.translation_password
  web_admin                       = var.web_admin
  web_portal                      = var.web_portal
}