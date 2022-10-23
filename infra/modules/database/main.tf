data "aws_ssm_parameter" "rds_password" {
  name = "/${var.app}/${var.environment}/db_password"
}

module "database" {
  source  = "terraform-aws-modules/rds/aws"
  version = "5.1.0"

  identifier        = "${var.app}-${var.environment}"
  engine            = "postgres"
  engine_version    = "14.1"
  instance_class    = var.db_instance_class
  allocated_storage = var.db_capacity

  family                      = "postgres14"
  major_engine_version        = "14"
  allow_major_version_upgrade = true
  auto_minor_version_upgrade  = true

  db_name  = "${var.app}-${var.environment}"
  port     = "5432"
  username = var.db_username
  password = data.aws_ssm_parameter.rds_password.value

  subnet_ids             = var.public_subnet_ids
  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = var.db_public_access

  skip_final_snapshot                 = true
  backup_retention_period             = var.db_backup_retention_days
  backup_window                       = var.db_backup_window
  maintenance_window                  = var.db_maintenance_window
  iam_database_authentication_enabled = true
}
