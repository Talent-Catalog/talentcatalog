output "database_url" {
  value = var.db_enable ? module.database[0].db_instance_endpoint : ""
}
