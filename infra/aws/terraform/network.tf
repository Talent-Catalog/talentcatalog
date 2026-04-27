module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "${var.app}-${var.env}-vpc"
  cidr = var.vpc_cidr

  azs              = var.availability_zones
  private_subnets  = var.private_subnet_cidr
  public_subnets   = var.public_subnet_cidr
  database_subnets = var.db_subnet_cidr
  create_igw       = true

  create_database_subnet_group           = true
  create_database_subnet_route_table     = true
  create_database_internet_gateway_route = true

  enable_nat_gateway = false
  enable_vpn_gateway = false

  enable_dns_hostnames = true
  enable_dns_support   = true
}