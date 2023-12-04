variable "app" {
  description = "Name of the application"
  type        = string
}

variable "env" {
  type        = string
  description = "Name of the environment"
}

variable "aws_access_key" {
  type        = string
  description = "AWS access key to be used by the java application"
}

variable "aws_secret_key" {
  type        = string
  description = "AWS secret key to be used by the java application"
}

variable "s3_bucket" {
  type        = string
  description = "S3 bucket name"
}

variable "es_password" {
  type        = string
  description = "ElasticSearch password"
}

variable "es_url" {
  type        = string
  description = "ElasticSearch URL"
}

variable "es_username" {
  type        = string
  description = "ElasticSearch username"
}

variable "email_default" {
  type        = string
  description = "Default email"
}

variable "email_password" {
  type        = string
  description = "Email password"
}

variable "email_test_override" {
  type        = string
  description = "Test override email"
}

variable "email_user" {
  type        = string
  description = "Email user"
}

variable "environment" {
  type        = string
  description = "Denotes running environment"
}

variable "drive_id" {
  type        = string
  description = "Google drive candidate drive ID"
}

variable "drive_rootfolder" {
  type        = string
  description = "Google drive root folder ID"
}

variable "drive_list_folders_id" {
  type        = string
  description = "Google drive list folders drive ID"
}

variable "drive_list_folders_root_id" {
  type        = string
  description = "Google drive list folders root ID"
}

variable "drive_private_key" {
  type        = string
  description = "Google drive private key"
}

variable "drive_private_key_id" {
  type        = string
  description = "Google drive private key ID"
}

variable "gradle_home" {
  type        = string
  description = "Gradle home directory"
}

variable "java_home" {
  type        = string
  description = "Java home directory"
}

variable "jwt_secret" {
  type        = string
  description = "JWT secret"
}

variable "m2" {
  type        = string
  description = "M2"
}

variable "m2_home" {
  type        = string
  description = "M2 home directory"
}

variable "server_port" {
  type        = string
  description = "Server port"
}

variable "server_url" {
  type        = string
  description = "Server URL"
}

variable "sf_base_classic_url" {
  type        = string
  description = "Salesforce Classic base url"
}

variable "sf_base_lightning_url" {
  type        = string
  description = "Salesforce Lightning base url"
}

variable "sf_base_login_url" {
  type        = string
  description = "Salesforce login base url - for obtaining an access token"
}

variable "sf_consumer_key" {
  type        = string
  description = "Salesforce connected app client ID, also for access token"
}
variable "sf_private_key" {
  type        = string
  description = "Salesforce private key"
}

variable "sf_user" {
  type        = string
  description = "Salesforce logging-in user for access token"
}

variable "slack_token" {
  type        = string
  description = "Slack token"
}

variable "spring_client_url" {
  type        = string
  description = "Spring boot admin client URL"
}

variable "spring_datasource_password" {
  type        = string
  description = "Spring datasource password"
}

variable "db_enable" {
  default     = false
  description = "Flag to define the app will use an existance database (e.g. test env) or create new one"
}

variable "database_url" {
  type        = string
  description = "Spring datasource URL"
}

variable "spring_datasource_url" {
  type        = string
  description = "Spring datasource URL"
}

variable "spring_datasource_username" {
  type        = string
  description = "Spring datasource username"
}

variable "spring_db_pool_max" {
  type        = string
  description = "Spring database max"
}

variable "spring_db_pool_min" {
  type        = string
  description = "Spring database min"
}

variable "spring_servlet_max_file_size" {
  type        = string
  description = "Spring servlet multipart max file size"
}

variable "spring_servlet_max_request_size" {
  type        = string
  description = "Spring servlet multipart max request size"
}

variable "tbb_cors_urls" {
  type        = string
  description = "TBB Cors URLs"
}

variable "tbb_db_copy_config" {
  type        = string
  description = "TBB partner DB copy config"
}

variable "translation_password" {
  type        = string
  description = "Translation password"
}

variable "web_admin" {
  type        = string
  description = "Web admin URL"
}

variable "web_portal" {
  type        = string
  description = "Candidate portal"
}
