# Security Groups
resource "aws_security_group" "fargate" {
  name   = "${var.app}-${var.env}-fargate-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    description      = "Container port"
    protocol         = "tcp"
    from_port        = var.container_port
    to_port          = var.container_port
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "alb" {
  name   = "${var.app}-${var.env}-alb-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    description      = "Http access port"
    protocol         = "tcp"
    from_port        = 80
    to_port          = 80
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  ingress {
    description      = "Https access port"
    protocol         = "tcp"
    from_port        = 443
    to_port          = 443
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# IAM Roles and Policies
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.app}-${var.env}-fargate-task-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy_attachment_ssm" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy_attachment_ecr" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# ALB
data "aws_acm_certificate" "certificate" {
  domain      = var.site_domain
  types       = ["AMAZON_ISSUED"]
  most_recent = true
}

module "alb" {
  source             = "terraform-aws-modules/alb/aws"
  version            = "~> 6.0"
  name               = "${var.app}-${var.env}"
  load_balancer_type = "application"

  vpc_id          = module.vpc.vpc_id
  subnets         = module.vpc.public_subnets
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
      targets = {}
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

# CloudWatch Logs
resource "aws_cloudwatch_log_group" "logs" {
  name              = "/fargate/service/${var.app}-${var.env}-fargate-log"
  retention_in_days = "14"
}

# ECS
data "aws_ssm_parameters_by_path" "secrets" {
  path = "/${var.app}/${var.env}/"
}

locals {
  env_secrets = [for name in data.aws_ssm_parameters_by_path.secrets.names : {
    "name" : split("/", name)[3],
    "value" : trimspace(data.aws_ssm_parameters_by_path.secrets.values[index(data.aws_ssm_parameters_by_path.secrets.names, name)])
  }]
}

module "ecs" {
  source = "terraform-aws-modules/ecs/aws"

  cluster_name = "${var.app}-${var.env}"

  cluster_configuration = {
    execute_command_configuration = {
      logging = "OVERRIDE"
      log_configuration = {
        cloud_watch_log_group_name = "/aws/ecs/aws-ec2"
      }
    }
  }

  fargate_capacity_providers = {
    FARGATE = {
      default_capacity_provider_strategy = {
        weight = 50
      }
    }
    FARGATE_SPOT = {
      default_capacity_provider_strategy = {
        weight = 50
      }
    }
  }
}

resource "aws_ecs_service" "web-app" {
  name            = "${var.app}-${var.env}"
  cluster         = module.ecs.cluster_id
  task_definition = aws_ecs_task_definition.web-app.arn
  desired_count   = var.ecs_tasks_count
  launch_type     = "FARGATE"

  load_balancer {
    target_group_arn = module.alb.target_group_arns[0]
    container_name   = "${var.app}-${var.env}"
    container_port   = var.container_port
  }

  network_configuration {
    security_groups = [aws_security_group.fargate.id]
    subnets         = module.vpc.public_subnets
    assign_public_ip = true
  }
  health_check_grace_period_seconds = 300
}

resource "aws_ecs_task_definition" "web-app" {
  family                   = "${var.app}-${var.env}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  cpu                      = 256
  memory                   = 2048
  container_definitions = jsonencode([
    {
      name                   = "${var.app}-${var.env}"
      image                  = var.container_image
      essential              = true
      readonlyRootFilesystem = false
      environment            = local.env_secrets
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/fargate/service/${var.app}-${var.env}-fargate-log"
          awslogs-stream-prefix = "ecs"
          awslogs-region        = "us-east-1"
        }
      }
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
        }
      ]
    }
  ])
}