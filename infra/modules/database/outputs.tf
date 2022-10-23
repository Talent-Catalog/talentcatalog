output "db_endpoint" {
  description = "The database endpoint, can be used to connect to the database"
  value       = module.database.db_instance_endpoint
}
