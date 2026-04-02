resource "aws_s3_bucket" "candidate_files" {
  count  = var.cloudfront_enable ? 1 : 0
  bucket = var.candidate_files_bucket

  tags = merge(
    {
      Name = var.candidate_files_bucket
    },
    var.common_tags,
    {
      Purpose = "Candidate attachments"
    }
  )
}

resource "aws_s3_bucket_public_access_block" "candidate_files" {
  count  = var.cloudfront_enable ? 1 : 0
  bucket = aws_s3_bucket.candidate_files[0].id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "candidate_files" {
  count  = var.cloudfront_enable ? 1 : 0
  bucket = aws_s3_bucket.candidate_files[0].id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "candidate_files" {
  count  = var.cloudfront_enable ? 1 : 0
  bucket = aws_s3_bucket.candidate_files[0].id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_policy" "candidate_files" {
  count  = var.cloudfront_enable ? 1 : 0
  bucket = aws_s3_bucket.candidate_files[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontRead"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.candidate_files[0].arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.main[0].arn
          }
        }
      }
    ]
  })
}

resource "aws_s3_bucket" "translations" {
  bucket = var.translations_bucket

  tags = merge(
    {
      Name = var.translations_bucket
    },
    var.common_tags,
    {
      Purpose = "Translations"
    }
  )
}

resource "aws_s3_bucket_public_access_block" "translations" {
  bucket = aws_s3_bucket.translations.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "translations" {
  bucket = aws_s3_bucket.translations.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "translations" {
  bucket = aws_s3_bucket.translations.id

  versioning_configuration {
    status = "Enabled"
  }
}
