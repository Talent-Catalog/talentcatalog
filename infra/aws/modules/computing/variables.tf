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

variable "private_subnet_ids" {
  type        = list(string)
  description = "VPC - Private Subnet ID"
}

variable "container_port" {
  type        = number
  description = "Container port"
}

variable "container_image" {
  type        = string
  description = "The ecr url for the docker image"
}

variable "certificate_domain" {
  type        = string
  description = "The primary domain name of the certificate in ACM"
}

variable "ecs_tasks_count" {
  type        = number
  description = "The desired number of ECS tasks"
}
