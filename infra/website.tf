module "main_website" {
  source      = "./modules"
  app         = var.app
  environment = var.environment
  providers = {
    aws = aws.main
  }
}

module "standby_website" {
  source      = "./modules"
  app         = var.app
  environment = var.environment
  providers = {
    aws = aws.standby
  }
}
