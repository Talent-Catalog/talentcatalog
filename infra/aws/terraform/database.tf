resource "aws_security_group" "db" {
  name   = "${var.app}-${var.env}-db-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    description = "Database endpoint port"
    protocol    = "tcp"
    from_port   = 5432
    to_port     = 5432
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

module "database" {
  count   = var.db_enable ? 1 : 0
  source  = "terraform-aws-modules/rds/aws"
  version = "5.1.0"

  identifier        = "${var.app}-${var.env}"
  engine            = "postgres"
  engine_version    = "14.3"
  instance_class    = var.db_instance_class
  allocated_storage = var.db_capacity

  family                      = "postgres14"
  major_engine_version        = "14"
  allow_major_version_upgrade = true
  auto_minor_version_upgrade  = true

  db_name                = var.app
  port                   = "5432"
  username               = var.spring_datasource_username
  password               = var.spring_datasource_password
  create_random_password = false

  db_subnet_group_name = module.vpc.database_subnet_group_name

  subnet_ids             = module.vpc.public_subnets
  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = var.db_public_access

  skip_final_snapshot                 = true
  backup_retention_period             = var.db_backup_retention_days
  backup_window                       = var.db_backup_window
  maintenance_window                  = var.db_maintenance_window
  iam_database_authentication_enabled = true

  multi_az = var.db_multi_az
}