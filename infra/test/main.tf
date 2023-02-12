terraform {
  required_version = "= 1.3.0"
}

# Store the terraform state remotely in S3 bucket
terraform {
  backend "s3" {
    bucket = "tbbtalent-terraform-state-test"
    key    = "terraform.tfstate"
    region = "us-east-1"
    # todo: add dynamodb for state lock
  }
}

module "website" {
  source            = "../modules"
  app               = "tctalent"
  env               = "test"
  site_domain       = "tctalent-test.org"
  container_image   = "231168606641.dkr.ecr.us-east-1.amazonaws.com/tbbtalentv2"
  container_port    = 8080
  db_multi_az       = false
  db_instance_class = "db.t3.micro"
}
