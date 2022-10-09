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
