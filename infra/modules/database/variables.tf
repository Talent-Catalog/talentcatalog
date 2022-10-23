variable "app" {
  description = "Name of the application"
  type = string
}

variable "vpc_id" {
  description = "VPC ID for the network"
}

variable "public_subnet_ids" {
  description = "VPC - Public Subnet ID"
}

variable "db_username" {
  description = "Master username of the db"
  default = "postgress-admin"
}

variable "db_backup_retention_days" {
  description = "Number of days to retain db backups, Needed to enable backup"
  default     = 7
}

variable "db_backup_window" {
  description = "The daily time range during which automated backups for rds are created. Time in UTC."
  default     = "13:00-15:00" # 00:00 - 02:00 AU time
}

variable "db_maintenance_window" {
  description = "The daily time range during which maintance for rds are started. Time in UTC."
  default     = "Sat:15:00-Sat:17:00" # Sunday 02:00 - 04:00 AU time
}

variable "db_capacity" {
  description = "The Aurora capacity unit of the db."
  default     = 20
}

variable "db_instance_class" {
  description = "The database db instance class"
  default     = "db.t3.micro"
}

variable "db_public_access" {
  description = "Flag to set if the database publicly accessible"
}
