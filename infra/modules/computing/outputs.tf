output "dns_name" {
  description = "The DNS name of the load balancer"
  value       = module.alb.lb_dns_name
}

output "zone_id" {
  description = "The zone id of the load balancer to assist with creating DNS records"
  value       = module.alb.lb_zone_id
}

output "arn" {
  description = "The ARN of the load balancer"
  value       = module.alb.lb_arn
}
