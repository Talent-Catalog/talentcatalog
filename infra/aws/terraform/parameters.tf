resource "aws_ssm_parameter" "aws_access_key" {
  name  = "/${var.app}/${var.env}/AWS_CREDENTIALS_ACCESSKEY"
  type  = "String"
  value = var.aws_access_key != null ? var.aws_access_key : "PLACEHOLDER_UPDATE_MANUALLY"

  # Prevent Terraform from overwriting the value after initial creation when the placeholder is
  # replaced manually from the AWS Console or CLI
  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "aws_secret_key" {
  name  = "/${var.app}/${var.env}/AWS_CREDENTIALS_SECRETKEY"
  type  = "SecureString"
  value = var.aws_secret_key != null ? var.aws_secret_key : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "s3_bucket" {
  name  = "/${var.app}/${var.env}/AWS_S3_BUCKETNAME"
  type  = "String"
  value = var.s3_bucket != null ? var.s3_bucket : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "duolingo_api_secret" {
  name  = "/${var.app}/${var.env}/DUOLINGO_API_APISECRET"
  type  = "SecureString"
  value = var.duolingo_api_secret != null ? var.duolingo_api_secret : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "es_password" {
  name  = "/${var.app}/${var.env}/ELASTICSEARCH_PASSWORD"
  type  = "SecureString"
  value = var.es_password != null ? var.es_password : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "es_url" {
  name  = "/${var.app}/${var.env}/ELASTICSEARCH_URL"
  type  = "String"
  value = var.es_url != null ? var.es_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "es_username" {
  name  = "/${var.app}/${var.env}/ELASTICSEARCH_USERNAME"
  type  = "String"
  value = var.es_username != null ? var.es_username : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "email_default" {
  name  = "/${var.app}/${var.env}/EMAIL_DEFAULTEMAIL"
  type  = "String"
  value = var.email_default != null ? var.email_default : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "email_password" {
  name  = "/${var.app}/${var.env}/EMAIL_PASSWORD"
  type  = "SecureString"
  value = var.email_password != null ? var.email_password : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "email_test_override" {
  name  = "/${var.app}/${var.env}/EMAIL_TESTOVERRIDEEMAIL"
  type  = "String"
  value = var.email_test_override != null ? var.email_test_override : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "email_user" {
  name  = "/${var.app}/${var.env}/EMAIL_USER"
  type  = "String"
  value = var.email_user != null ? var.email_user : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "environment" {
  name  = "/${var.app}/${var.env}/ENVIRONMENT"
  type  = "String"
  value = var.environment != null ? var.environment : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "drive_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_CANDIDATEDATADRIVEID"
  type  = "SecureString"
  value = var.drive_id != null ? var.drive_id : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "drive_rootfolder" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_CANDIDATEROOTFOLDERID"
  type  = "SecureString"
  value = var.drive_rootfolder != null ? var.drive_rootfolder : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "drive_list_folders_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_LISTFOLDERSDRIVEID"
  type  = "SecureString"
  value = var.drive_list_folders_id != null ? var.drive_list_folders_id : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "drive_list_folders_root_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_LISTFOLDERSROOTID"
  type  = "SecureString"
  value = var.drive_list_folders_root_id != null ? var.drive_list_folders_root_id : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "drive_private_key" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_PRIVATEKEY"
  type  = "SecureString"
  value = var.drive_private_key != null ? var.drive_private_key : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "drive_private_key_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_PRIVATEKEYID"
  type  = "SecureString"
  value = var.drive_private_key_id != null ? var.drive_private_key_id : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "gradle_home" {
  name  = "/${var.app}/${var.env}/GRADLE_HOME"
  type  = "String"
  value = var.gradle_home != null ? var.gradle_home : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "java_home" {
  name  = "/${var.app}/${var.env}/JAVA_HOME"
  type  = "String"
  value = var.java_home != null ? var.java_home : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "jwt_secret" {
  name  = "/${var.app}/${var.env}/JWT_SECRET"
  type  = "SecureString"
  value = var.jwt_secret != null ? var.jwt_secret : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "m2" {
  name  = "/${var.app}/${var.env}/M2"
  type  = "String"
  value = var.m2 != null ? var.m2 : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "m2_home" {
  name  = "/${var.app}/${var.env}/M2_HOME"
  type  = "String"
  value = var.m2_home != null ? var.m2_home : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "preset_api_token" {
  name  = "/${var.app}/${var.env}/PRESET_API_TOKEN"
  type  = "SecureString"
  value = var.preset_api_token != null ? var.preset_api_token : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "preset_secret" {
  name  = "/${var.app}/${var.env}/PRESET_SECRET"
  type  = "SecureString"
  value = var.preset_secret != null ? var.preset_secret : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "preset_workspace_id" {
  name  = "/${var.app}/${var.env}/PRESET_WORKSPACE_ID"
  type  = "SecureString"
  value = var.preset_workspace_id != null ? var.preset_workspace_id : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "redis_host" {
  name  = "/${var.app}/${var.env}/REDIS_HOST"
  type  = "String"
  value = var.redis_host != null ? var.redis_host : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "redis_port" {
  name  = "/${var.app}/${var.env}/REDIS_PORT"
  type  = "String"
  value = var.redis_port != null ? var.redis_port : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "server_port" {
  name  = "/${var.app}/${var.env}/SERVER_PORT"
  type  = "String"
  value = var.server_port != null ? var.server_port : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "server_url" {
  name  = "/${var.app}/${var.env}/SERVER_URL"
  type  = "String"
  value = var.server_url != null ? var.server_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "sf_base_classic_url" {
  name  = "/${var.app}/${var.env}/SF_BASE_CLASSIC_URL"
  type  = "String"
  value = var.sf_base_classic_url != null ? var.sf_base_classic_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "sf_base_lightning_url" {
  name  = "/${var.app}/${var.env}/SF_BASE_LIGHTNING_URL"
  type  = "String"
  value = var.sf_base_lightning_url != null ? var.sf_base_lightning_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "sf_base_login_url" {
  name  = "/${var.app}/${var.env}/SF_BASE_LOGIN_URL"
  type  = "String"
  value = var.sf_base_login_url != null ? var.sf_base_login_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "sf_consumer_key" {
  name  = "/${var.app}/${var.env}/SF_CONSUMER_KEY"
  type  = "SecureString"
  value = var.sf_consumer_key != null ? var.sf_consumer_key : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "sf_private_key" {
  name  = "/${var.app}/${var.env}/SF_PRIVATE_KEY"
  type  = "SecureString"
  value = var.sf_private_key != null ? var.sf_private_key : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "sf_user" {
  name  = "/${var.app}/${var.env}/SF_USER"
  type  = "SecureString"
  value = var.sf_user != null ? var.sf_user : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "slack_token" {
  name  = "/${var.app}/${var.env}/SLACK_TOKEN"
  type  = "SecureString"
  value = var.slack_token != null ? var.slack_token : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_client_url" {
  name  = "/${var.app}/${var.env}/SPRING_BOOT_ADMIN_CLIENT_URL"
  type  = "String"
  value = var.spring_client_url != null ? var.spring_client_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_datasource_password" {
  name  = "/${var.app}/${var.env}/SPRING_DATASOURCE_PASSWORD"
  type  = "SecureString"
  value = var.spring_datasource_password != null ? var.spring_datasource_password : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_datasource_url" {
  name  = "/${var.app}/${var.env}/SPRING_DATASOURCE_URL"
  type  = "String"
  value = var.db_enable ? "jdbc:postgresql://${module.database[0].db_instance_endpoint}/tbbtalent" : (var.spring_datasource_url != null ? var.spring_datasource_url : "PLACEHOLDER_UPDATE_MANUALLY")

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_datasource_username" {
  name  = "/${var.app}/${var.env}/SPRING_DATASOURCE_USERNAME"
  type  = "String"
  value = var.spring_datasource_username != null ? var.spring_datasource_username : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_db_pool_max" {
  name  = "/${var.app}/${var.env}/SPRING_DBPOOL_MAX"
  type  = "String"
  value = var.spring_db_pool_max != null ? var.spring_db_pool_max : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_db_pool_min" {
  name  = "/${var.app}/${var.env}/SPRING_DBPOOL_MIN"
  type  = "String"
  value = var.spring_db_pool_min != null ? var.spring_db_pool_min : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_servlet_max_file_size" {
  name  = "/${var.app}/${var.env}/SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE"
  type  = "String"
  value = var.spring_servlet_max_file_size != null ? var.spring_servlet_max_file_size : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "spring_servlet_max_request_size" {
  name  = "/${var.app}/${var.env}/SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE"
  type  = "String"
  value = var.spring_servlet_max_request_size != null ? var.spring_servlet_max_request_size : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "tc_api_key" {
  name  = "/${var.app}/${var.env}/TC_API_KEY"
  type  = "SecureString"
  value = var.tc_api_key != null ? var.tc_api_key : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "tc_api_url" {
  name  = "/${var.app}/${var.env}/TC_API_URL"
  type  = "String"
  value = var.tc_api_url != null ? var.tc_api_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "tc_cors_urls" {
  name  = "/${var.app}/${var.env}/TC_CORS_URLS"
  type  = "String"
  value = var.tc_cors_urls != null ? var.tc_cors_urls : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "tc_db_copy_config" {
  name  = "/${var.app}/${var.env}/TC_PARTNER_DBCOPY_CONFIG"
  type  = "String"
  value = var.tc_db_copy_config != null ? var.tc_db_copy_config : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "tc_destinations" {
  name  = "/${var.app}/${var.env}/TC_DESTINATIONS"
  type  = "String"
  value = var.tc_destinations != null ? var.tc_destinations : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "tc_skills_extraction_api_url" {
  name  = "/${var.app}/${var.env}/TC_SKILLS_EXTRACTION_API_URL"
  type  = "String"
  value = var.tc_skills_extraction_api_url != null ? var.tc_skills_extraction_api_url : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "translation_password" {
  name  = "/${var.app}/${var.env}/TRANSLATION_PASSWORD"
  type  = "SecureString"
  value = var.translation_password != null ? var.translation_password : "PLACEHOLDER_UPDATE_MANUALLY"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "web_admin" {
  name  = "/${var.app}/${var.env}/WEB_ADMIN"
  type  = "String"
  value = var.web_admin
}

resource "aws_ssm_parameter" "web_portal" {
  name  = "/${var.app}/${var.env}/WEB_PORTAL"
  type  = "String"
  value = var.web_portal
}