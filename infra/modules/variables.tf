variable "app" {
  description = "Name of the application"
  type        = string
}

variable "environment" {
  description = "Name of the application environment. e.g. dev, prod, test, staging"
  type        = string
}

variable "db_public_access" {
  description = "Flag to set if the database publicly accessible"
  default     = true
}
