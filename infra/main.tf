terraform {
  required_version = "= 1.3.0"
}

# Store the terraform state remotely in S3 bucket
terraform {
  backend "s3" {
    bucket               = "tbbtalent-terraform-state"
    key                  = "terraform.tfstate"
    region               = "us-east-1"
    workspace_key_prefix = "tbbtalent"
  }
}

module "website" {
  source            = "./modules"
  app               = var.app
  site_domain       = var.site_domain
  container_image   = var.container_image
  container_port    = var.container_port
  db_multi_az       = var.db_multi_az
  db_instance_class = var.db_instance_class
}
