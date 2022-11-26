variable "app" {
  type        = string
  description = "Name of the application"
}

variable "db_public_access" {
  default     = true
  description = "Flag to set if the database publicly accessible"
}

variable "db_enable" {
  default     = false
  description = "Flag to define the app will use an existance database (e.g. test env) or create new one"
}

variable "db_multi_az" {
  type        = bool
  description = "Flag to define if database is multiaz"
}

variable "db_instance_class" {
  type        = string
  description = "The database db instance class"
}

variable "site_domain" {
  type        = string
  description = "The domain name of the website"
}

variable "container_image" {
  type        = string
  description = "The ecr url for the docker image"
}

variable "container_port" {
  type        = number
  description = "Container port"
}
