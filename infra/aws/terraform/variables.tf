### Terraform variables for AWS infrastructure:

### ECS container and general application variables:

variable "app" {
  type        = string
  description = "Name of the application"
}

variable "env" {
  type        = string
  description = "Name of the environment"
}

variable "common_tags" {
  type        = map(string)
  description = "Common tags to apply to resources"
  default     = {}
}

variable "site_domain" {
  type        = string
  description = "The domain name of the website"
}

variable "container_image" {
  type        = string
  description = "The ECR URL for the Docker image"
}

variable "ecr_repository_name" {
  type        = string
  description = "The name of the ECR repository"
  default     = "tc-core"
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

variable "fargate_cpu" {
  type        = number
  description = "Fargate task CPU units (1024 = 1 vCPU)"
  default     = 256
}

variable "fargate_memory" {
  type        = number
  description = "Fargate task memory in MiB"
  default     = 2048
}

### Database configuration variables:

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

variable "db_engine_version" {
  type        = string
  description = "The PostgreSQL engine version"
  default     = "14.3"
}

variable "db_family" {
  type        = string
  description = "The database parameter group family"
  default     = "postgres14"
}

variable "db_major_engine_version" {
  type        = string
  description = "The major version of the database engine"
  default     = "14"
}

variable "db_name" {
  type        = string
  description = "The name of the database to create (alphanumeric only)"
  default     = "tctalent"
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

### Redis Cache (ElastiCache) variables:

variable "cache_enable" {
  type        = bool
  default     = false
  description = "Flag to enable Redis ElastiCache cluster creation"
}

variable "cache_cluster_id" {
  type        = string
  description = "The Redis cluster identifier"
  default     = null
}

variable "cache_node_type" {
  type        = string
  description = "The compute and memory capacity of the cache nodes (e.g., cache.t3.micro)"
  default     = "cache.t3.micro"
}

variable "cache_num_cache_nodes" {
  type        = number
  description = "The number of cache nodes (replicas)"
  default     = 1
}

variable "cache_engine_version" {
  type        = string
  description = "The Redis engine version"
  default     = "7.1"
}

variable "cache_port" {
  type        = number
  description = "The port number on which the cache accepts connections"
  default     = 6379
}

### Network configuration variables:

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

### Spring application configuration variables:
### All values are provided by the environment's main.tf (non-secrets)
### or passed through from secrets.auto.tfvars (secrets).

# --- Non-secret parameters (values provided directly in main.tf) ---

variable "s3_bucket" {
  type        = string
  description = "S3 bucket name"
}

variable "translations_bucket" {
  type        = string
  description = "S3 bucket name for translations"
}

variable "translations_folder" {
  type        = string
  description = "S3 folder/prefix for translations"
}

variable "s3_region" {
  type        = string
  description = "AWS region for the Spring S3 client (s3.region)"
  default     = "eu-west-2"
}

variable "environment" {
  type        = string
  description = "Denotes running environment; must match app enum values (local, staging, prod)"
}

variable "email_default" {
  type        = string
  description = "Default email address"
}

variable "email_test_override" {
  type        = string
  description = "Test override email address"
}

variable "email_user" {
  type        = string
  description = "Email user"
}

variable "email_type" {
  type        = string
  description = "Email type (e.g., SMTP)"
}

variable "es_url" {
  type        = string
  description = "Elasticsearch URL (todo: retire elasticsearch)"
}

variable "es_username" {
  type        = string
  description = "Elasticsearch username (todo: retire elasticsearch)"
}

variable "gradle_home" {
  type        = string
  description = "Gradle home directory"
}

variable "java_home" {
  type        = string
  description = "Java home directory"
}

variable "logbuilder_include_cpu_utilization" {
  type        = string
  description = "LogBuilder flag to include CPU utilization metrics"
}

variable "logbuilder_include_memory_utilization" {
  type        = string
  description = "LogBuilder flag to include memory utilization metrics"
}

variable "m2" {
  type        = string
  description = "Maven binary path"
}

variable "m2_home" {
  type        = string
  description = "Maven home directory"
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
  description = "Salesforce login base URL"
}

variable "spring_client_url" {
  type        = string
  description = "Spring Boot admin client URL"
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

variable "tc_api_url" {
  type        = string
  description = "TC API URL"
}

variable "tc_cors_urls" {
  type        = string
  description = "TC CORS URLs (comma-separated)"
}

variable "tc_db_copy_config" {
  type        = string
  description = "TC partner DB copy config"
}

variable "tc_destinations" {
  type        = string
  description = "TC destinations configuration"
}

variable "tc_skills_extraction_api_url" {
  type        = string
  description = "TC skills extraction API URL"
}

variable "web_admin" {
  type        = string
  description = "Web admin portal URL"
}

variable "web_portal" {
  type        = string
  description = "Candidate portal URL"
}

variable "tc_instance_type" {
  type        = string
  description = "TC instance type (e.g. TBB)"
  default     = "TBB"
}

# --- Auto-populated parameters (conditionally managed by Terraform) ---

variable "redis_host" {
  type        = string
  description = "Redis host (auto-populated from ElastiCache when cache_enable=true)"
  default     = ""
}

variable "redis_port" {
  type        = string
  description = "Redis port (auto-populated from ElastiCache when cache_enable=true)"
  default     = ""
}

variable "spring_datasource_url" {
  type        = string
  description = "Spring datasource URL (auto-populated from RDS when db_enable=true)"
  default     = ""
}

# --- Secret parameters (values loaded from secrets.auto.tfvars) ---

variable "aws_access_key" {
  type        = string
  description = "AWS access key for S3 access"
  sensitive   = true
}

variable "aws_secret_key" {
  type        = string
  description = "AWS secret key for S3 access"
  sensitive   = true
}

variable "duolingo_api_secret" {
  type        = string
  description = "Duolingo API secret"
  sensitive   = true
}

variable "es_password" {
  type        = string
  description = "Elasticsearch password (todo: retire elasticsearch)"
  sensitive   = true
}

variable "email_password" {
  type        = string
  description = "Email password"
  sensitive   = true
}

variable "drive_id" {
  type        = string
  description = "Google Drive candidate data drive ID"
  sensitive   = true
}

variable "drive_rootfolder" {
  type        = string
  description = "Google Drive candidate root folder ID"
  sensitive   = true
}

variable "drive_list_folders_id" {
  type        = string
  description = "Google Drive list folders drive ID"
  sensitive   = true
}

variable "drive_list_folders_root_id" {
  type        = string
  description = "Google Drive list folders root ID"
  sensitive   = true
}

variable "drive_private_key" {
  type        = string
  description = "Google Drive private key (PEM format with literal \\n)"
  sensitive   = true
}

variable "drive_private_key_id" {
  type        = string
  description = "Google Drive private key ID"
  sensitive   = true
}

variable "jwt_secret" {
  type        = string
  description = "JWT secret"
  sensitive   = true
}

variable "preset_api_token" {
  type        = string
  description = "Preset API token"
  sensitive   = true
}

variable "preset_secret" {
  type        = string
  description = "Preset secret"
  sensitive   = true
}

variable "preset_workspace_id" {
  type        = string
  description = "Preset workspace ID"
  sensitive   = true
}

variable "sf_consumer_key" {
  type        = string
  description = "Salesforce connected app consumer key"
  sensitive   = true
}

variable "sf_private_key" {
  type        = string
  description = "Salesforce private key"
  sensitive   = true
}

variable "sf_user" {
  type        = string
  description = "Salesforce user for access token"
  sensitive   = true
}

variable "slack_channel_id" {
  type        = string
  description = "Slack channel ID for job registration posts (e.g. C048GS1KHPG test, C029WMY6H1U live)"
}

variable "slack_token" {
  type        = string
  description = "Slack token"
  sensitive   = true
}

variable "spring_datasource_password" {
  type        = string
  description = "Spring datasource (RDS) password"
  sensitive   = true
}

variable "tc_api_key" {
  type        = string
  description = "TC API key"
  sensitive   = true
}

variable "translation_password" {
  type        = string
  description = "Translation password"
  sensitive   = true
}

variable "tc_boot_admin_password" {
  type        = string
  description = "System admin password for TC"
  sensitive   = true
  default     = ""
}

variable "cloudfront_enable" {
  type        = bool
  default     = false
  description = "Enable CloudFront distribution in front of ALB + S3 candidate files bucket"
}

variable "candidate_files_bucket" {
  type        = string
  default     = ""
  description = "S3 bucket name for candidate file attachments (required when cloudfront_enable=true)"
}
