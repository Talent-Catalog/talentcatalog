variable "app" {
  type        = string
  description = "Name of the application"
  default     = "tctalent"
}

variable "site_domain" {
  type        = string
  description = "The domain for the website"
  default     = "tctalent-test.org"
}

variable "db_instance_class" {
  type        = string
  description = "The database db instance class"
  default     = "db.t3.micro"
}

variable "db_multi_az" {
  type        = bool
  description = "Flag to define if database is multi az"
  default     = false
}

variable "container_image" {
  type        = string
  description = "The ecr url for the docker image"
  default     = "231168606641.dkr.ecr.us-east-1.amazonaws.com/tbbtalentv2"
}

variable "container_port" {
  type        = number
  description = "Container port"
  default     = 8080
}
