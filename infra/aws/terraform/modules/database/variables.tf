variable "app" {
  type        = string
  description = "Name of the application"
}

variable "env" {
  type        = string
  description = "Name of the environment"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID for the network"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "VPC - Public Subnet ID"
}

variable "db_subnet_group_name" {
  type        = string
  description = "VPC subnet group name for the database"
}

variable "db_backup_retention_days" {
  type        = number
  description = "Number of days to retain db backups, Needed to enable backup"
  default     = 7
}

variable "db_backup_window" {
  type        = string
  description = "The daily time range during which automated backups for rds are created. Time in UTC."
  default     = "13:00-15:00" # 00:00 - 02:00 AU time
}

variable "db_maintenance_window" {
  type        = string
  description = "The daily time range during which maintance for rds are started. Time in UTC."
  default     = "Sat:15:00-Sat:17:00" # Sunday 02:00 - 04:00 AU time
}

variable "db_capacity" {
  type        = number
  description = "The Aurora capacity unit of the db."
  default     = 20
}

variable "db_instance_class" {
  type        = string
  description = "The database db instance class"
}

variable "db_public_access" {
  type        = bool
  description = "Flag to set if the database publicly accessible"
}

variable "db_enable" {
  type        = bool
  description = "Flag to define if database is reuqired"
}

variable "db_multi_az" {
  type        = bool
  description = "Flag to define if database is multiaz"
}

variable "db_username" {
  type        = string
  description = "The database username"
}

variable "db_password" {
  type        = string
  description = "The database password"
}
