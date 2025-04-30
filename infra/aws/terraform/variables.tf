variable "app" {
  type        = string
  description = "Name of the application"
}

variable "env" {
  type        = string
  description = "Name of the environment"
}

variable "db_public_access" {
  default     = true
  description = "Flag to set if the database publicly accessible"
}

variable "db_enable" {
  default     = false
  description = "Flag to define the app will use an existing database (e.g., test env) or create a new one"
}

variable "db_multi_az" {
  type        = bool
  description = "Flag to define if database is multi-AZ"
}

variable "db_instance_class" {
  type        = string
  description = "The database instance class"
}

variable "site_domain" {
  type        = string
  description = "The domain name of the website"
}

variable "container_image" {
  type        = string
  description = "The ECR URL for the Docker image"
}

variable "container_port" {
  type        = number
  description = "Container port"
}

variable "ecs_tasks_count" {
  type        = number
  description = "The desired number of ECS tasks"
  default     = 1
}

variable "aws_access_key" {
  type        = string
  description = "AWS access key to be used by the Java application"
}

variable "aws_secret_key" {
  type        = string
  description = "AWS secret key to be used by the Java application"
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
  description = "Google Drive candidate drive ID"
}

variable "drive_rootfolder" {
  type        = string
  description = "Google Drive root folder ID"
}

variable "drive_list_folders_id" {
  type        = string
  description = "Google Drive list folders drive ID"
}

variable "drive_list_folders_root_id" {
  type        = string
  description = "Google Drive list folders root ID"
}

variable "drive_private_key" {
  type        = string
  description = "Google Drive private key"
}

variable "drive_private_key_id" {
  type        = string
  description = "Google Drive private key ID"
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
  description = "Salesforce Classic base URL"
}

variable "sf_base_lightning_url" {
  type        = string
  description = "Salesforce Lightning base URL"
}

variable "sf_base_login_url" {
  type        = string
  description = "Salesforce login base URL - for obtaining an access token"
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
  description = "Spring Boot admin client URL"
}

variable "spring_datasource_url" {
  type        = string
  description = "Spring datasource URL"
}

variable "spring_datasource_password" {
  type        = string
  description = "Spring datasource password"
}

variable "spring_datasource_username" {
  type        = string
  description = "Spring datasource username"
}

variable "spring_db_pool_max" {
  type        = string
  description = "Spring database max pool size"
}

variable "spring_db_pool_min" {
  type        = string
  description = "Spring database min pool size"
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
  description = "TBB CORS URLs"
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
  description = "Candidate portal URL"
}

variable "vpc_cidr" {
  type        = string
  description = "The CIDR block for the VPC"
  default     = "172.32.0.0/16"
}

variable "private_subnet_cidr" {
  type        = list(string)
  description = "List of CIDR blocks for private subnets"
  default     = ["172.32.0.0/20", "172.32.16.0/20", "172.32.32.0/20"]
}

variable "public_subnet_cidr" {
  type        = list(string)
  description = "List of CIDR blocks for public subnets"
  default     = ["172.32.48.0/20", "172.32.64.0/20", "172.32.80.0/20"]
}

variable "db_subnet_cidr" {
  type        = list(string)
  description = "List of CIDR blocks for database subnets"
  default     = ["172.32.96.0/20", "172.32.112.0/20", "172.32.128.0/20"]
}

variable "availability_zones" {
  type        = list(string)
  description = "List of availability zones"
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "db_backup_retention_days" {
  type        = number
  description = "Number of days to retain DB backups"
  default     = 7
}

variable "db_backup_window" {
  type        = string
  description = "The daily time range during which automated backups for RDS are created (UTC)"
  default     = "13:00-15:00"
}

variable "db_maintenance_window" {
  type        = string
  description = "The daily time range during which maintenance for RDS is started (UTC)"
  default     = "Sat:15:00-Sat:17:00"
}

variable "db_capacity" {
  type        = number
  description = "The Aurora capacity unit of the DB"
  default     = 20
}