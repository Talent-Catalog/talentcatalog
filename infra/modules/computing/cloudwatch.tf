resource "aws_cloudwatch_log_group" "logs" {
  name              = "/fargate/service/${var.app}-${terraform.workspace}-fargate-log"
  retention_in_days = "14"
}
