variable "app" {
  type = string
}

variable "env" {
  type = string
}

variable "site_domain" {
  type = string
}

variable "container_image" {
  type = string
}

variable "container_port" {
  type = number
}

variable "db_enable" {
  type = bool
}

variable "db_public_access" {
  type = bool
}

variable "db_multi_az" {
  type = bool
}

variable "ecs_tasks_count" {
  type = number
}

variable "db_instance_class" {
  type = string
}

variable "aws_access_key" {
  type = string
}

variable "aws_secret_key" {
  type = string
}

variable "s3_bucket" {
  type = string
}

variable "es_password" {
  type = string
}

variable "es_url" {
  type = string
}

variable "es_username" {
  type = string
}

variable "email_default" {
  type = string
}

variable "email_password" {
  type = string
}

variable "email_test_override" {
  type = string
}

variable "email_user" {
  type = string
}

variable "environment" {
  type = string
}

variable "drive_id" {
  type = string
}

variable "drive_rootfolder" {
  type = string
}

variable "drive_list_folders_id" {
  type = string
}

variable "drive_list_folders_root_id" {
  type = string
}

variable "drive_private_key" {
  type = string
}

variable "drive_private_key_id" {
  type = string
}

variable "gradle_home" {
  type = string
}

variable "java_home" {
  type = string
}

variable "jwt_secret" {
  type = string
}

variable "m2" {
  type = string
}

variable "m2_home" {
  type = string
}

variable "server_port" {
  type = string
}

variable "server_url" {
  type = string
}

variable "sf_base_classic_url" {
  type = string
}

variable "sf_base_lightning_url" {
  type = string
}

variable "sf_base_login_url" {
  type = string
}

variable "sf_consumer_key" {
  type = string
}

variable "sf_private_key" {
  type = string
}

variable "sf_user" {
  type = string
}

variable "slack_token" {
  type = string
}

variable "spring_client_url" {
  type = string
}

variable "spring_datasource_password" {
  type = string
}

variable "spring_datasource_url" {
  type = string
}

variable "spring_datasource_username" {
  type = string
}

variable "spring_db_pool_max" {
  type = string
}

variable "spring_db_pool_min" {
  type = string
}

variable "spring_servlet_max_file_size" {
  type = string
}

variable "spring_servlet_max_request_size" {
  type = string
}

variable "tbb_cors_urls" {
  type = string
}

variable "tbb_db_copy_config" {
  type = string
}

variable "translation_password" {
  type = string
}

variable "web_admin" {
  type = string
}

variable "web_portal" {
  type = string
}