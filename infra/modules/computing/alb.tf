# Get the HTTPS certificate for the website
data "aws_acm_certificate" "certificate" {
  domain      = var.certificate_domain
  types       = ["AMAZON_ISSUED"]
  most_recent = true
}

module "alb" {
  source             = "terraform-aws-modules/alb/aws"
  version            = "~> 6.0"
  name               = "${var.app}-${var.env}"
  load_balancer_type = "application"

  vpc_id          = var.vpc_id
  subnets         = var.public_subnet_ids
  security_groups = [aws_security_group.alb.id]
  target_groups = [
    {
      name_prefix      = "app-"
      backend_protocol = "HTTP"
      backend_port     = var.container_port
      target_type      = "ip"
      health_check = {
        enabled             = true
        interval            = 65
        path                = "/"
        port                = "traffic-port"
        healthy_threshold   = 2
        unhealthy_threshold = 5
        timeout             = 60
        protocol            = "HTTP"
        matcher             = "200,302"
      }
      targets = {
      }
    }
  ]

  https_listeners = [
    {
      port               = 443
      protocol           = "HTTPS"
      certificate_arn    = data.aws_acm_certificate.certificate.arn
      target_group_index = 0
    }
  ]

  http_tcp_listeners = [
    {
      port        = 80
      protocol    = "HTTP"
      action_type = "redirect"
      redirect = {
        port        = "443"
        protocol    = "HTTPS"
        status_code = "HTTP_301"
      }
    }
  ]
}
