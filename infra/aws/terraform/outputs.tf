output "dns_name" {
  description = "The DNS name of the load balancer"
  value       = module.alb.lb_dns_name
}

output "zone_id" {
  description = "The zone ID of the load balancer to assist with creating DNS records"
  value       = module.alb.lb_zone_id
}

output "arn" {
  description = "The ARN of the load balancer"
  value       = module.alb.lb_arn
}

output "database_url" {
  description = "The endpoint URL of the database"
  value       = var.db_enable ? module.database[0].db_instance_endpoint : ""
}