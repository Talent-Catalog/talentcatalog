# Redis ElastiCache Configuration

# Security Group for Redis
resource "aws_security_group" "redis" {
  count = var.cache_enable ? 1 : 0

  name        = "${var.app}-${var.env}-redis-sg"
  description = "Security group for Redis ElastiCache"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "Redis port from ECS tasks"
    from_port       = var.cache_port
    to_port         = var.cache_port
    protocol        = "tcp"
    security_groups = [aws_security_group.fargate.id]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, {
    Name = "${var.app}-${var.env}-redis-sg"
  })
}

# ElastiCache Subnet Group
resource "aws_elasticache_subnet_group" "redis" {
  count = var.cache_enable ? 1 : 0

  name       = "${var.app}-${var.env}-redis-subnet-group"
  subnet_ids = module.vpc.private_subnets

  tags = merge(var.common_tags, {
    Name = "${var.app}-${var.env}-redis-subnet-group"
  })
}

# ElastiCache Replication Group (Redis Cluster)
resource "aws_elasticache_replication_group" "redis" {
  count = var.cache_enable ? 1 : 0

  replication_group_id      = var.cache_cluster_id != null ? var.cache_cluster_id : "${var.app}-${var.env}-redis"
  description               = "Redis cluster for ${var.app}-${var.env}"
  engine                    = "redis"
  engine_version            = var.cache_engine_version
  node_type                 = var.cache_node_type
  num_cache_clusters        = var.cache_num_cache_nodes
  port                      = var.cache_port
  parameter_group_name      = "default.redis7"
  subnet_group_name         = aws_elasticache_subnet_group.redis[0].name
  security_group_ids        = [aws_security_group.redis[0].id]
  automatic_failover_enabled = var.cache_num_cache_nodes > 1 ? true : false
  multi_az_enabled          = var.cache_num_cache_nodes > 1 ? true : false
  
  # Maintenance and backup settings
  maintenance_window        = "sun:05:00-sun:07:00"
  snapshot_window           = "03:00-04:00"
  snapshot_retention_limit  = 5
  
  # Enable automatic minor version upgrades
  auto_minor_version_upgrade = true
  
  # Encryption disabled for simpler application connectivity
  at_rest_encryption_enabled = false
  transit_encryption_enabled = false

  tags = merge(var.common_tags, {
    Name = var.cache_cluster_id != null ? var.cache_cluster_id : "${var.app}-${var.env}-redis"
  })
}
