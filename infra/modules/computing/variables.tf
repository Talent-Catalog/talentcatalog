variable "app" {
  description = "Name of the application"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID for the network"
}

variable "public_subnet_ids" {
  description = "VPC - Public Subnet ID"
}

variable "private_subnet_ids" {
  description = "VPC - Private Subnet ID"
}

variable "container_port" {
  description = "Container port"
  default     = 8080
}

variable "container_image" {
  description = "Container image"
  default     = "231168606641.dkr.ecr.us-east-1.amazonaws.com/tbbtalentv2"
}
