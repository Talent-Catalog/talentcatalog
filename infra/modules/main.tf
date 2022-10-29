module "webesite_network" {
  source = "./network"
  app    = var.app
}

module "computing" {
  source             = "./computing"
  app                = var.app
  vpc_id             = module.webesite_network.vpc_id
  public_subnet_ids  = module.webesite_network.vpc_public_subnets
  private_subnet_ids = module.webesite_network.vpc_private_subnets
}

module "database" {
  source               = "./database"
  app                  = var.app
  db_public_access     = var.db_public_access
  vpc_id               = module.webesite_network.vpc_id
  public_subnet_ids    = module.webesite_network.vpc_public_subnets
  db_subnet_group_name = module.webesite_network.db_subnet_group_name
}
