module "website" {
  source      = "./modules"
  app         = var.app
  environment = var.environment
  providers = {
    aws = aws.main
  }
}
