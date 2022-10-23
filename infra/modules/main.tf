module "webesite_secrets" {
  source      = "./secrets"
  app         = var.app
  environment = var.environment
}

module "webesite_network" {
  source      = "./network"
  app         = var.app
  environment = var.environment
}

module "database" {
  source            = "./database"
  app               = var.app
  environment       = var.environment
  db_public_access  = var.db_public_access
  vpc_id            = module.webesite_network.vpc_id
  public_subnet_ids = module.webesite_network.vpc_public_subnets
}
