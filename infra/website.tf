module "website" {
  source      = "./modules"
  app         = var.app
  site_domain = "tctalent-test.org"
  providers = {
    aws = aws.main
  }
}
