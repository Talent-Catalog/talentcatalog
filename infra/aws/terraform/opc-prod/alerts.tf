# SNS topic for CloudWatch alarm notifications (TC-1526).
#
# This topic lives in this root rather than the shared module because grn-prod runs in the same
# AWS account (289896345557) and would otherwise create a duplicate topic. grn-prod instead looks
# this topic up by name via a data "aws_sns_topic" source (see grn-prod/main.tf) - this root must
# be applied before grn-prod.
#
# STABLE NAME CONTRACT: other repos (tc-api, tc-skills-extraction-service) look up this topic by
# NAME ("opc-prod-alarms") at deploy/runtime. Do not rename this resource's `name`.
resource "aws_sns_topic" "alarms" {
  name = "opc-prod-alarms"

  # No KMS encryption in this change. If encryption is added later it MUST use a customer-managed
  # KMS key whose key policy grants cloudwatch.amazonaws.com kms:Decrypt and
  # kms:GenerateDataKey* - CloudWatch cannot publish to topics encrypted with the AWS-managed
  # alias/aws/sns key.

  tags = merge(
    { Name = "opc-prod-alarms" },
    local.common_tags,
    {
      Component = "sns"
      Purpose   = "cloudwatch-alerting"
    }
  )
}

# Email addresses to notify on alarm. Each address must confirm the subscription after apply.
# Confirm using `aws sns confirm-subscription` with --authenticate-on-unsubscribe true, not by
# clicking the link, so a stray unsubscribe click can't remove the group.
variable "alarm_email_subscriptions" {
  type        = list(string)
  description = "Email addresses to subscribe to the opc-prod-alarms SNS topic"
  default     = ["aws-alerts@openpathwaycollective.org"]
}

resource "aws_sns_topic_subscription" "alarm_emails" {
  for_each = toset(var.alarm_email_subscriptions)

  topic_arn = aws_sns_topic.alarms.arn
  protocol  = "email"
  endpoint  = each.value
}

output "alarm_sns_topic_arn" {
  description = "ARN of the opc-prod CloudWatch alarm SNS topic (TC-1526)"
  value       = aws_sns_topic.alarms.arn
}
