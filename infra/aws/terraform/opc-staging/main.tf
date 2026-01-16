# This file is the main entry to the opc-staging infrastructure
# The idea of using a separate directory is to use different remote state backends and roles
terraform {
  required_version = ">= 1.3.0"
}

# Store the terraform state remotely in S3 bucket
terraform {
  backend "s3" {
    bucket         = "opc-shared-terraform-state"
    key            = "staging/talentcatalog/terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "opc-terraform-locks"
    encrypt        = "true"
  }
}

provider "aws" {
  region = "eu-west-2"

  assume_role {
    role_arn = "arn:aws:iam::164804461258:role/opc-staging-terraform-exec"
  }
}
