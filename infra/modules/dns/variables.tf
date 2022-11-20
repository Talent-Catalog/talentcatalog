variable "site_domain" {
  type        = string
  description = "The primary domain name of the website"
}

variable "lb_dns_name" {
  type        = string
  description = "The DNS name of the load balancer"
}

variable "lb_zone_id" {
  type        = string
  description = "The zone id of the load balancer"
}
