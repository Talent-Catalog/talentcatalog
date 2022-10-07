terraform {
  required_version = ">= 1.1.2"
}

terraform {
  backend "s3" {
    bucket = "tbbtalent-terraform-state"
    key    = "tbbtalent"
    region = "us-east-1"
  }
}
