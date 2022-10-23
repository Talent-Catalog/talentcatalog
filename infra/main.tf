terraform {
  required_version = "= 1.3.0"
}

terraform {
  backend "s3" {
    bucket               = "tbbtalent-terraform-state"
    key                  = "terraform.tfstate"
    region               = "us-east-1"
    workspace_key_prefix = "tbbtalent"
  }
}

