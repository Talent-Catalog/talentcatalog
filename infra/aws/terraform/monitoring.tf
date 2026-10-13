# CloudWatch alarms for the ALB and RDS instance created above (TC-1526 - Vanta CloudWatch alarm
# coverage for ALB, RDS and Lambda).
#
# Alarms are only created when the calling root passes alarm_sns_topic_arn (non-null). Roots that
# don't set it (e.g. prod/, test/) get zero alarms and are otherwise unaffected.
#
# Lambda: the OPC accounts have no failing Lambda functions, so no Lambda alarms are added here.

locals {
  name_prefix = "${var.app}-${var.env}"

  alb_alarms_enabled = var.alarm_sns_topic_arn != null
  rds_alarms_enabled = var.alarm_sns_topic_arn != null && var.db_enable

  # RDS instance class -> total instance memory in bytes, used to derive the FreeableMemory alarm
  # threshold as a percentage of RAM. Extend this when a new db_instance_class is used with alarms
  # enabled.
  rds_instance_memory_bytes = {
    "db.t3.medium" = 4 * 1024 * 1024 * 1024 # 4 GiB
    "db.m6g.large" = 8 * 1024 * 1024 * 1024 # 8 GiB
  }

  # lookup() (never errors, unlike map[key] indexing) resolves to null for an unlisted class.
  # That's fine to compute unconditionally - including for roots with RDS alarms disabled, e.g. a
  # legacy TBB class in prod/, test/ - because leniency here is purely "don't error while building
  # this value"; whether an unresolved value is actually ACCEPTABLE is decided separately below,
  # and is only lenient when alarm_sns_topic_arn is null (no alarms being created). When alarms
  # are enabled, the precondition on aws_cloudwatch_metric_alarm.rds_freeable_memory hard-fails the
  # plan for an unlisted class instead of silently using a wrong or null threshold.
  rds_instance_memory_bytes_resolved = lookup(local.rds_instance_memory_bytes, var.db_instance_class, null)

  rds_freeable_memory_threshold_bytes = (
    var.rds_freeable_memory_threshold_bytes_override != null
    ? var.rds_freeable_memory_threshold_bytes_override
    : (
      local.rds_instance_memory_bytes_resolved != null
      ? local.rds_instance_memory_bytes_resolved * var.rds_freeable_memory_threshold_percent
      : null
    )
  )

  rds_free_storage_space_threshold_bytes = var.db_capacity * 1024 * 1024 * 1024 * var.rds_free_storage_space_threshold_percent
}

# --- ALB alarms ------------------------------------------------------------------------------
# LoadBalancer dimension is the ALB's ARN suffix ("app/<name>/<id>"), not its name.

resource "aws_cloudwatch_metric_alarm" "alb_unhealthy_hosts" {
  count = local.alb_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-alb-unhealthy-hosts"
  alarm_description = "ALB ${local.name_prefix}: one or more targets in the target group are failing health checks. TC-1526."

  namespace   = "AWS/ApplicationELB"
  metric_name = "UnHealthyHostCount"
  dimensions = {
    TargetGroup  = module.alb.target_group_arn_suffixes[0]
    LoadBalancer = module.alb.lb_arn_suffix
  }

  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 5
  datapoints_to_alarm = 3
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.alb_unhealthy_host_count_threshold
  treat_missing_data  = "notBreaching"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-alb-unhealthy-hosts" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

resource "aws_cloudwatch_metric_alarm" "alb_latency_p95" {
  count = local.alb_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-alb-latency-p95"
  alarm_description = "ALB ${local.name_prefix}: p95 target response time exceeds ${var.alb_target_response_time_p95_threshold_seconds}s. TC-1526."

  namespace   = "AWS/ApplicationELB"
  metric_name = "TargetResponseTime"
  dimensions = {
    LoadBalancer = module.alb.lb_arn_suffix
  }

  extended_statistic  = "p95"
  period              = 60
  evaluation_periods  = 5
  datapoints_to_alarm = 5
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.alb_target_response_time_p95_threshold_seconds
  treat_missing_data  = "notBreaching"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-alb-latency-p95" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

resource "aws_cloudwatch_metric_alarm" "alb_5xx_target" {
  count = local.alb_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-alb-5xx-target"
  alarm_description = "ALB ${local.name_prefix}: target (application) 5XX responses exceed ${var.alb_target_5xx_count_threshold} in 5 minutes. TC-1526."

  namespace   = "AWS/ApplicationELB"
  metric_name = "HTTPCode_Target_5XX_Count"
  dimensions = {
    LoadBalancer = module.alb.lb_arn_suffix
  }

  statistic           = "Sum"
  period              = 300
  evaluation_periods  = 1
  datapoints_to_alarm = 1
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.alb_target_5xx_count_threshold
  treat_missing_data  = "notBreaching"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-alb-5xx-target" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

resource "aws_cloudwatch_metric_alarm" "alb_5xx_elb" {
  count = local.alb_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-alb-5xx-elb"
  alarm_description = "ALB ${local.name_prefix}: load-balancer-generated 5XX responses exceed ${var.alb_elb_5xx_count_threshold} in 5 minutes. TC-1526."

  namespace   = "AWS/ApplicationELB"
  metric_name = "HTTPCode_ELB_5XX_Count"
  dimensions = {
    LoadBalancer = module.alb.lb_arn_suffix
  }

  statistic           = "Sum"
  period              = 300
  evaluation_periods  = 1
  datapoints_to_alarm = 1
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.alb_elb_5xx_count_threshold
  treat_missing_data  = "notBreaching"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-alb-5xx-elb" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

# --- RDS alarms ------------------------------------------------------------------------------
# DBInstanceIdentifier matches the `identifier` argument passed to module.database below
# ("${var.app}-${var.env}") - there is no module output exposing it directly.

resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  count = local.rds_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-rds-cpu"
  alarm_description = "RDS ${local.name_prefix}: CPUUtilization exceeds ${var.rds_cpu_utilization_threshold_percent}%. TC-1526."

  namespace   = "AWS/RDS"
  metric_name = "CPUUtilization"
  dimensions = {
    DBInstanceIdentifier = local.name_prefix
  }

  statistic           = "Average"
  period              = 300
  evaluation_periods  = 3
  datapoints_to_alarm = 3
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.rds_cpu_utilization_threshold_percent
  treat_missing_data  = "missing"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-rds-cpu" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_freeable_memory" {
  count = local.rds_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-rds-freeable-memory"
  alarm_description = "RDS ${local.name_prefix}: FreeableMemory below ${var.rds_freeable_memory_threshold_percent * 100}% of instance RAM (${var.db_instance_class}). TC-1526."

  namespace   = "AWS/RDS"
  metric_name = "FreeableMemory"
  dimensions = {
    DBInstanceIdentifier = local.name_prefix
  }

  statistic           = "Average"
  period              = 300
  evaluation_periods  = 3
  datapoints_to_alarm = 3
  comparison_operator = "LessThanThreshold"
  threshold           = local.rds_freeable_memory_threshold_bytes
  treat_missing_data  = "missing"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  lifecycle {
    # Leniency for an unlisted db_instance_class applies ONLY when no alarm is being created for
    # this root (var.alarm_sns_topic_arn == null); this resource only exists at all when that's
    # non-null (see count above), so in practice that first disjunct is always true here and is
    # included only so the condition reads correctly as a standalone statement of intent. Once
    # alarms are enabled, either the class must be known or an explicit override must be set -
    # otherwise this hard-fails the plan, naming the offending class, instead of silently applying
    # threshold = null (which, since `threshold` is an Optional provider argument, would otherwise
    # risk being silently omitted/defaulted rather than erroring).
    precondition {
      condition = (
        var.alarm_sns_topic_arn == null
        || var.rds_freeable_memory_threshold_bytes_override != null
        || local.rds_instance_memory_bytes_resolved != null
      )
      error_message = "Unknown db_instance_class '${var.db_instance_class}' for the FreeableMemory alarm threshold (TC-1526). Add it to local.rds_instance_memory_bytes in monitoring.tf, or set rds_freeable_memory_threshold_bytes_override explicitly."
    }
  }

  tags = merge(
    { Name = "${local.name_prefix}-rds-freeable-memory" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_free_storage_space" {
  count = local.rds_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-rds-free-storage"
  alarm_description = "RDS ${local.name_prefix}: FreeStorageSpace below ${var.rds_free_storage_space_threshold_percent * 100}% of allocated storage (${var.db_capacity} GB). TC-1526."

  namespace   = "AWS/RDS"
  metric_name = "FreeStorageSpace"
  dimensions = {
    DBInstanceIdentifier = local.name_prefix
  }

  statistic           = "Minimum"
  period              = 300
  evaluation_periods  = 1
  datapoints_to_alarm = 1
  comparison_operator = "LessThanThreshold"
  threshold           = local.rds_free_storage_space_threshold_bytes
  treat_missing_data  = "missing"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-rds-free-storage" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_disk_queue_depth" {
  count = local.rds_alarms_enabled ? 1 : 0

  alarm_name        = "${local.name_prefix}-rds-disk-queue-depth"
  alarm_description = "RDS ${local.name_prefix}: DiskQueueDepth exceeds ${var.rds_disk_queue_depth_threshold}, indicating storage I/O contention. TC-1526."

  namespace   = "AWS/RDS"
  metric_name = "DiskQueueDepth"
  dimensions = {
    DBInstanceIdentifier = local.name_prefix
  }

  statistic           = "Average"
  period              = 300
  evaluation_periods  = 3
  datapoints_to_alarm = 3
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.rds_disk_queue_depth_threshold
  treat_missing_data  = "missing"

  alarm_actions = [var.alarm_sns_topic_arn]
  ok_actions    = [var.alarm_sns_topic_arn]

  tags = merge(
    { Name = "${local.name_prefix}-rds-disk-queue-depth" },
    var.common_tags,
    { Component = "cloudwatch-alarm" }
  )
}
