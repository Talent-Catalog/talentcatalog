terraform {
  required_version = ">= 1.3.0"
}

locals {
  dont_run_here = tobool("false")
}

# Intentionally invalid so `terraform plan` fails fast if run here.
resource "null_resource" "do_not_run_here" {
  triggers = {
    error = "Do not run Terraform from infra/aws/terraform. Run from env folders (e.g. infra/aws/terraform/opc-staging)."
  }
}
