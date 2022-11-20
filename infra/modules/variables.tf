variable "app" {
  description = "Name of the application"
  type        = string
}

variable "db_public_access" {
  description = "Flag to set if the database publicly accessible"
  default     = true
}

variable "db_enable" {
  description = "Flag to define if database is reuqired"
  default     = false
}

variable "site_domain" {
  type        = string
  description = "The primary domain name of the website"
}
