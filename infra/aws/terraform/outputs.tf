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

output "redis_primary_endpoint" {
  description = "The primary endpoint address of the Redis cluster"
  value       = var.cache_enable ? aws_elasticache_replication_group.redis[0].primary_endpoint_address : ""
}

output "redis_port" {
  description = "The port number of the Redis cluster"
  value       = var.cache_enable ? var.cache_port : ""
}

output "redis_connection_string" {
  description = "The full Redis connection string (host:port)"
  value       = var.cache_enable ? "${aws_elasticache_replication_group.redis[0].primary_endpoint_address}:${var.cache_port}" : ""
}

output "ecr_repository_url" {
  description = "The URL of the ECR repository"
  value       = aws_ecr_repository.app.repository_url
}

output "ecr_repository_arn" {
  description = "The ARN of the ECR repository"
  value       = aws_ecr_repository.app.arn
}

output "ecr_repository_name" {
  description = "The name of the ECR repository"
  value       = aws_ecr_repository.app.name
}

output "cloudfront_distribution_id" {
  description = "The ID of the CloudFront distribution"
  value       = var.cloudfront_enable ? aws_cloudfront_distribution.main[0].id : ""
}

output "cloudfront_domain_name" {
  description = "The CloudFront domain name"
  value       = var.cloudfront_enable ? aws_cloudfront_distribution.main[0].domain_name : ""
}

output "candidate_files_bucket_name" {
  description = "Candidate files S3 bucket name"
  value       = var.cloudfront_enable ? aws_s3_bucket.candidate_files[0].bucket : ""
}