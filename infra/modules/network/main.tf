module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "${var.app}-${terraform.workspace}-vpc"
  cidr = var.vpc_cidr

  azs             = var.availability_zones
  private_subnets = var.private_subnet_cidr
  public_subnets  = var.public_subnet_cidr
  create_igw      = true

  enable_nat_gateway = false
  enable_vpn_gateway = false

  enable_dns_hostnames = true
  enable_dns_support   = true
}
