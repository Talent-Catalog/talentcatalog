resource "aws_ssm_parameter" "aws_access_key" {
  name  = "/${var.app}/${var.env}/AWS_CREDENTIALS_ACCESSKEY"
  type  = "String"
  value = var.aws_access_key
}

resource "aws_ssm_parameter" "aws_secret_key" {
  name  = "/${var.app}/${var.env}/AWS_CREDENTIALS_SECRETKEY"
  type  = "SecureString"
  value = var.aws_secret_key
}

resource "aws_ssm_parameter" "s3_bucket" {
  name  = "/${var.app}/${var.env}/AWS_S3_BUCKETNAME"
  type  = "String"
  value = var.s3_bucket
}

resource "aws_ssm_parameter" "es_password" {
  name  = "/${var.app}/${var.env}/ELASTICSEARCH_PASSWORD"
  type  = "SecureString"
  value = var.es_password
}

resource "aws_ssm_parameter" "es_url" {
  name  = "/${var.app}/${var.env}/ELASTICSEARCH_URL"
  type  = "String"
  value = var.es_url
}

resource "aws_ssm_parameter" "es_username" {
  name  = "/${var.app}/${var.env}/ELASTICSEARCH_USERNAME"
  type  = "String"
  value = var.es_username
}

resource "aws_ssm_parameter" "email_default" {
  name  = "/${var.app}/${var.env}/EMAIL_DEFAULTEMAIL"
  type  = "String"
  value = var.email_default
}

resource "aws_ssm_parameter" "email_password" {
  name  = "/${var.app}/${var.env}/EMAIL_PASSWORD"
  type  = "SecureString"
  value = var.email_password
}

resource "aws_ssm_parameter" "email_test_override" {
  name  = "/${var.app}/${var.env}/EMAIL_TESTOVERRIDEEMAIL"
  type  = "String"
  value = var.email_test_override
}

resource "aws_ssm_parameter" "email_user" {
  name  = "/${var.app}/${var.env}/EMAIL_USER"
  type  = "String"
  value = var.email_user
}

resource "aws_ssm_parameter" "environment" {
  name  = "/${var.app}/${var.env}/ENVIRONMENT"
  type  = "String"
  value = var.environment
}

resource "aws_ssm_parameter" "drive_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_CANDIDATEDATADRIVEID"
  type  = "SecureString"
  value = var.drive_id
}

resource "aws_ssm_parameter" "drive_rootfolder" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_CANDIDATEROOTFOLDERID"
  type  = "SecureString"
  value = var.drive_rootfolder
}

resource "aws_ssm_parameter" "drive_list_folders_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_LISTFOLDERSDRIVEID"
  type  = "SecureString"
  value = var.drive_list_folders_id
}

resource "aws_ssm_parameter" "drive_list_folders_root_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_LISTFOLDERSROOTID"
  type  = "SecureString"
  value = var.drive_list_folders_root_id
}

resource "aws_ssm_parameter" "drive_private_key" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_PRIVATEKEY"
  type  = "SecureString"
  value = var.drive_private_key
}

resource "aws_ssm_parameter" "drive_private_key_id" {
  name  = "/${var.app}/${var.env}/GOOGLE_DRIVE_PRIVATEKEYID"
  type  = "SecureString"
  value = var.drive_private_key_id
}

resource "aws_ssm_parameter" "gradle_home" {
  name  = "/${var.app}/${var.env}/GRADLE_HOME"
  type  = "String"
  value = var.gradle_home
}

resource "aws_ssm_parameter" "java_home" {
  name  = "/${var.app}/${var.env}/JAVA_HOME"
  type  = "String"
  value = var.java_home
}

resource "aws_ssm_parameter" "jwt_secret" {
  name  = "/${var.app}/${var.env}/JWT_SECRET"
  type  = "SecureString"
  value = var.jwt_secret
}

resource "aws_ssm_parameter" "m2" {
  name  = "/${var.app}/${var.env}/M2"
  type  = "String"
  value = var.m2
}

resource "aws_ssm_parameter" "m2_home" {
  name  = "/${var.app}/${var.env}/M2_HOME"
  type  = "String"
  value = var.m2_home
}

resource "aws_ssm_parameter" "server_port" {
  name  = "/${var.app}/${var.env}/SERVER_PORT"
  type  = "String"
  value = var.server_port
}

resource "aws_ssm_parameter" "server_url" {
  name  = "/${var.app}/${var.env}/SERVER_URL"
  type  = "String"
  value = var.server_url
}

resource "aws_ssm_parameter" "sf_base_classic_url" {
  name  = "/${var.app}/${var.env}/SF_BASE_CLASSIC_URL"
  type  = "String"
  value = var.sf_base_classic_url
}

resource "aws_ssm_parameter" "sf_base_lightning_url" {
  name  = "/${var.app}/${var.env}/SF_BASE_LIGHTNING_URL"
  type  = "String"
  value = var.sf_base_lightning_url
}

resource "aws_ssm_parameter" "sf_base_login_url" {
  name  = "/${var.app}/${var.env}/SF_BASE_LOGIN_URL"
  type  = "String"
  value = var.sf_base_login_url
}

resource "aws_ssm_parameter" "sf_consumer_key" {
  name  = "/${var.app}/${var.env}/SF_CONSUMER_KEY"
  type  = "SecureString"
  value = var.sf_consumer_key
}

resource "aws_ssm_parameter" "sf_private_key" {
  name  = "/${var.app}/${var.env}/SF_PRIVATE_KEY"
  type  = "SecureString"
  value = var.sf_private_key
}

resource "aws_ssm_parameter" "sf_user" {
  name  = "/${var.app}/${var.env}/SF_USER"
  type  = "SecureString"
  value = var.sf_user
}

resource "aws_ssm_parameter" "slack_token" {
  name  = "/${var.app}/${var.env}/SLACK_TOKEN"
  type  = "SecureString"
  value = var.slack_token
}

resource "aws_ssm_parameter" "spring_client_url" {
  name  = "/${var.app}/${var.env}/SPRING_BOOT_ADMIN_CLIENT_URL"
  type  = "String"
  value = var.spring_client_url
}

resource "aws_ssm_parameter" "spring_datasource_password" {
  name  = "/${var.app}/${var.env}/SPRING_DATASOURCE_PASSWORD"
  type  = "SecureString"
  value = var.spring_datasource_password
}

resource "aws_ssm_parameter" "spring_datasource_url" {
  name  = "/${var.app}/${var.env}/SPRING_DATASOURCE_URL"
  type  = "String"
  value = "jdbc:postgresql://${var.db_enable ? module.database[0].db_instance_endpoint : var.spring_datasource_url}/tbbtalent"
}

resource "aws_ssm_parameter" "spring_datasource_username" {
  name  = "/${var.app}/${var.env}/SPRING_DATASOURCE_USERNAME"
  type  = "String"
  value = var.spring_datasource_username
}

resource "aws_ssm_parameter" "spring_db_pool_max" {
  name  = "/${var.app}/${var.env}/SPRING_DBPOOL_MAX"
  type  = "String"
  value = var.spring_db_pool_max
}

resource "aws_ssm_parameter" "spring_db_pool_min" {
  name  = "/${var.app}/${var.env}/SPRING_DBPOOL_MIN"
  type  = "String"
  value = var.spring_db_pool_min
}

resource "aws_ssm_parameter" "spring_servlet_max_file_size" {
  name  = "/${var.app}/${var.env}/SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE"
  type  = "String"
  value = var.spring_servlet_max_file_size
}

resource "aws_ssm_parameter" "spring_servlet_max_request_size" {
  name  = "/${var.app}/${var.env}/SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE"
  type  = "String"
  value = var.spring_servlet_max_request_size
}

resource "aws_ssm_parameter" "tbb_cors_urls" {
  name  = "/${var.app}/${var.env}/TBB_CORS_URLS"
  type  = "String"
  value = var.tbb_cors_urls
}

resource "aws_ssm_parameter" "tbb_db_copy_config" {
  name  = "/${var.app}/${var.env}/TBB_PARTNER_DBCOPY_CONFIG"
  type  = "String"
  value = var.tbb_db_copy_config
}

resource "aws_ssm_parameter" "translation_password" {
  name  = "/${var.app}/${var.env}/TRANSLATION_PASSWORD"
  type  = "SecureString"
  value = var.translation_password
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