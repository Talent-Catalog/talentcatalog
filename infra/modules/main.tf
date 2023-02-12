# The network components (VPC, subnets, Internet gateway, etc..)
module "network" {
  source = "./network"
  app    = var.app
  env    = var.env
}

# The RDS Postgresql database
module "database" {
  depends_on = [
    module.network
  ]
  source               = "./database"
  app                  = var.app
  env                  = var.env
  db_enable            = var.db_enable
  db_multi_az          = var.db_multi_az
  db_public_access     = var.db_public_access
  db_instance_class    = var.db_instance_class
  vpc_id               = module.network.vpc_id
  public_subnet_ids    = module.network.vpc_public_subnets
  db_subnet_group_name = module.network.db_subnet_group_name
}

# The fargate service, with load balanacer (HTTP, HTTPS listners)
module "computing" {
  depends_on = [
    module.database
  ]
  source             = "./computing"
  app                = var.app
  env                = var.env
  vpc_id             = module.network.vpc_id
  public_subnet_ids  = module.network.vpc_public_subnets
  private_subnet_ids = module.network.vpc_private_subnets
  certificate_domain = var.site_domain
  container_image    = var.container_image
  container_port     = var.container_port
  ecs_tasks_count    = var.ecs_tasks_count
}

# Add DNS record to Route53
module "dns" {
  depends_on = [
    module.computing
  ]
  source      = "./dns"
  env         = var.env
  site_domain = var.site_domain
  lb_dns_name = module.computing.dns_name
  lb_zone_id  = module.computing.zone_id
}
