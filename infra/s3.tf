# Product images uploaded through the admin UI. The bucket is private; the app
# reads and writes it with its instance role and serves the images itself.

resource "aws_s3_bucket" "images" {
  bucket        = "${local.name}-images"
  force_destroy = true # empty the bucket on destroy

  tags = { Name = "${local.name}-images" }
}

resource "aws_s3_bucket_public_access_block" "images" {
  bucket = aws_s3_bucket.images.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "images" {
  bucket = aws_s3_bucket.images.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
