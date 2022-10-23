module "website" {
  source = "./modules"
  app    = var.app
  providers = {
    aws = aws.main
  }
}
