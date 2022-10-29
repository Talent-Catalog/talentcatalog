data "aws_ssm_parameters_by_path" "secrets" {
  path = "/${var.app}/develop/" // todo: change develop to terraform.workspace
}

locals {
  env_secrets = [for name in data.aws_ssm_parameters_by_path.secrets.names : {
    "name" : split("/", name)[3],                                                                                        // item name
    "value" : data.aws_ssm_parameters_by_path.secrets.values[index(data.aws_ssm_parameters_by_path.secrets.names, name)] // item value
  }]
}

module "ecs" {
  source = "terraform-aws-modules/ecs/aws"

  cluster_name = "${var.app}-${terraform.workspace}"

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
  name            = "${var.app}-${terraform.workspace}"
  cluster         = module.ecs.cluster_id
  task_definition = aws_ecs_task_definition.web-app.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  load_balancer {
    target_group_arn = module.alb.target_group_arns[0]
    container_name   = "${var.app}-${terraform.workspace}"
    container_port   = var.container_port
  }

  network_configuration {
    security_groups = [
      aws_security_group.fargate.id
    ]
    subnets          = var.private_subnet_ids
    assign_public_ip = true
  }
  health_check_grace_period_seconds = 300
}

resource "aws_ecs_task_definition" "web-app" {
  family                   = "${var.app}-${terraform.workspace}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  cpu                      = 256
  memory                   = 2048
  container_definitions = jsonencode([
    {
      name                   = "${var.app}-${terraform.workspace}"
      image                  = "968457613372.dkr.ecr.us-east-1.amazonaws.com/tbbtalentv2:latest"
      essential              = true
      readonlyRootFilesystem = false
      environment            = local.env_secrets
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "${var.app}-${terraform.workspace}"
          awslogs-stream-prefix = "ecs"
          awslogs-region        = "us-east-1"
        }
      }
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = 8080
        }
      ]
    }
  ])
}
