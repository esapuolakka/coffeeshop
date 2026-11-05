data "aws_ssm_parameter" "ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-${local.is_arm ? "arm64" : "x86_64"}"
}

resource "aws_instance" "app" {
  ami                    = data.aws_ssm_parameter.ami.value
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.app.name

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    region      = var.region
    secret_path = local.secret_path
    app_image   = var.app_image
    db_url      = "jdbc:postgresql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/${aws_db_instance.main.db_name}?sslmode=require"
    db_username = aws_db_instance.main.username
    admin_user  = var.admin_username
    bucket      = aws_s3_bucket.images.id
  })
  # a changed image or setting replaces the instance; the Elastic IP follows
  user_data_replace_on_change = true

  metadata_options {
    http_tokens = "required" # IMDSv2 only
    # 2, not 1: the app runs in a container (one extra network hop) and needs
    # the metadata service to get its S3 credentials.
    http_put_response_hop_limit = 2
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = 20
    encrypted   = true
  }

  tags = { Name = "${local.name}-app" }

  lifecycle {
    # a newer "latest" AMI must not replace a running instance
    ignore_changes = [ami]
  }

  depends_on = [
    aws_internet_gateway.main,
    # The first-boot script needs outbound access (packages, SSM, registry), and
    # Terraform removes a security group's default egress rule. Without this the
    # instance can boot before the rules exist and its script fails for good.
    aws_vpc_security_group_egress_rule.app_all,
    aws_vpc_security_group_ingress_rule.app_http,
    aws_ssm_parameter.db_password,
    aws_ssm_parameter.admin_password,
    aws_ssm_parameter.remember_me_key,
    aws_iam_role_policy.app,
  ]
}

# A stable address that survives instance replacement.
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  depends_on = [aws_internet_gateway.main]

  tags = { Name = "${local.name}-app" }
}
