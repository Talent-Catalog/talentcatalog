resource "aws_cloudwatch_log_group" "logs" {
  name              = "/fargate/service/${var.app}-${var.env}-fargate-log"
  retention_in_days = "14"
}
