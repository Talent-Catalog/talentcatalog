resource "aws_ssm_parameter" "website_token" {
  name      = "/${var.app}/${terraform.workspace}/website_token"
  type      = "SecureString"
  value     = "123ABC"
  overwrite = true
}
