data "aws_cloudfront_cache_policy" "caching_optimized" {
  count = var.cloudfront_enable ? 1 : 0
  name  = "Managed-CachingOptimized"
}

data "aws_cloudfront_cache_policy" "caching_disabled" {
  count = var.cloudfront_enable ? 1 : 0
  name  = "Managed-CachingDisabled"
}

data "aws_cloudfront_origin_request_policy" "all_viewer" {
  count = var.cloudfront_enable ? 1 : 0
  name  = "Managed-AllViewer"
}

resource "aws_cloudfront_origin_access_control" "candidate_files" {
  count                             = var.cloudfront_enable ? 1 : 0
  name                              = "${var.app}-${var.env}-candidate-files-oac"
  description                       = "CloudFront access to ${var.candidate_files_bucket}"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "main" {
  count   = var.cloudfront_enable ? 1 : 0
  enabled = true
  comment = "${var.app}-${var.env} distribution"

  aliases = [var.site_domain]

  origin {
    domain_name = module.alb.lb_dns_name
    origin_id   = "app-alb"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  origin {
    domain_name              = aws_s3_bucket.candidate_files[0].bucket_regional_domain_name
    origin_id                = "candidate-files-s3"
    origin_access_control_id = aws_cloudfront_origin_access_control.candidate_files[0].id
  }

  ordered_cache_behavior {
    path_pattern           = "/o/*"
    target_origin_id       = "candidate-files-s3"
    viewer_protocol_policy = "redirect-to-https"

    allowed_methods = ["GET", "HEAD", "OPTIONS"]
    cached_methods  = ["GET", "HEAD"]
    compress        = true

    cache_policy_id = data.aws_cloudfront_cache_policy.caching_optimized[0].id
  }

  default_cache_behavior {
    target_origin_id       = "app-alb"
    viewer_protocol_policy = "redirect-to-https"

    allowed_methods = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods  = ["GET", "HEAD"]
    compress        = true

    cache_policy_id          = data.aws_cloudfront_cache_policy.caching_disabled[0].id
    origin_request_policy_id = data.aws_cloudfront_origin_request_policy.all_viewer[0].id
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.cloudfront[0].certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  tags = merge(
    {
      Name = "cloudfront-${var.app}-${var.env}"
    },
    var.common_tags,
    {
      Component = "cloudfront"
    }
  )
}
