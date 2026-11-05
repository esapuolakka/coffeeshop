# Offline tests: run with `terraform test` (no AWS account or credentials needed).
# The providers are mocked, so these check the configuration's logic and its
# security-relevant settings, not what AWS would do with it.

mock_provider "aws" {
  mock_data "aws_caller_identity" {
    defaults = { account_id = "123456789012" }
  }
  mock_data "aws_availability_zones" {
    # deliberately unsorted
    defaults = { names = ["eu-north-1c", "eu-north-1a", "eu-north-1b"] }
  }
  mock_data "aws_ec2_instance_type_offerings" {
    defaults = { locations = ["eu-north-1b", "eu-north-1c"] }
  }
  mock_data "aws_ssm_parameter" {
    defaults = { value = "ami-0123456789abcdef0" }
  }
  mock_data "aws_iam_policy_document" {
    defaults = { json = "{\"Version\":\"2012-10-17\",\"Statement\":[]}" }
  }
  mock_resource "aws_iam_role" {
    defaults = { arn = "arn:aws:iam::123456789012:role/mock" }
  }
}

mock_provider "random" {}

mock_provider "http" {
  mock_data "http" {
    defaults = { response_body = "203.0.113.7\n" }
  }
}

run "defaults" {
  command = apply

  assert {
    condition     = aws_instance.app.instance_type == "t3.small" && aws_db_instance.main.instance_class == "db.t4g.micro"
    error_message = "Default sizes should be t3.small and db.t4g.micro."
  }

  assert {
    condition     = aws_db_instance.main.engine == "postgres" && startswith(aws_db_instance.main.engine_version, "15")
    error_message = "The database should be PostgreSQL 15."
  }

  assert {
    condition     = aws_subnet.public.availability_zone == "eu-north-1b"
    error_message = "The web server's subnet must be in a zone that offers the instance type."
  }

  assert {
    condition     = length(distinct(aws_subnet.private[*].availability_zone)) == 2
    error_message = "RDS needs private subnets in two different zones."
  }

  assert {
    condition     = contains(keys(aws_vpc_security_group_ingress_rule.app_http), "0.0.0.0/0") && aws_vpc_security_group_ingress_rule.app_http["0.0.0.0/0"].from_port == 80
    error_message = "HTTP should be open to the world by default."
  }

  assert {
    condition     = alltrue([for r in values(aws_vpc_security_group_ingress_rule.app_http) : r.from_port == 80 && r.to_port == 80])
    error_message = "Only port 80 may be open on the web server (no SSH)."
  }

  assert {
    condition     = aws_vpc_security_group_ingress_rule.db_from_app.from_port == 5432
    error_message = "The database must accept PostgreSQL traffic from the web server."
  }

  assert {
    condition     = aws_db_instance.main.publicly_accessible == false && aws_db_instance.main.storage_encrypted == true
    error_message = "The database must be private and encrypted."
  }

  assert {
    condition     = aws_db_instance.main.skip_final_snapshot == true && aws_db_instance.main.deletion_protection == false
    error_message = "The demo database must be easy to destroy."
  }

  assert {
    condition     = aws_s3_bucket.images.force_destroy == true && aws_s3_bucket_public_access_block.images.block_public_acls == true && aws_s3_bucket_public_access_block.images.restrict_public_buckets == true
    error_message = "The image bucket must be private and destroyable."
  }

  assert {
    condition     = one(aws_instance.app.metadata_options).http_tokens == "required" && one(aws_instance.app.metadata_options).http_put_response_hop_limit == 2
    error_message = "IMDSv2 only, with hop limit 2 so the container can reach it."
  }

  assert {
    condition     = data.aws_ssm_parameter.ami.name == "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
    error_message = "t3 is x86_64, so the x86_64 AMI is expected."
  }

  assert {
    condition     = strcontains(aws_instance.app.user_data, "docker pull \"ghcr.io/esapuolakka/coffeeshop-mvc:latest\"") && strcontains(aws_instance.app.user_data, "APP_STORAGE_TYPE=s3") && strcontains(aws_instance.app.user_data, "-p 80:8080")
    error_message = "user_data should start the configured image with S3 storage on port 80."
  }

  assert {
    condition     = strcontains(aws_instance.app.user_data, "get_secret admin_password") && !strcontains(aws_instance.app.user_data, random_password.admin.result) && !strcontains(aws_instance.app.user_data, random_password.db.result)
    error_message = "Secrets must be fetched at boot, never embedded in user_data."
  }

  assert {
    condition     = aws_instance.app.user_data_replace_on_change == true
    error_message = "Changing the image or settings should replace the instance."
  }

  assert {
    condition     = startswith(aws_ssm_parameter.admin_password.name, "/coffeeshop-") && aws_ssm_parameter.admin_password.type == "SecureString"
    error_message = "Secrets must be SecureString parameters under the deployment's own path."
  }
}

run "restrict_to_my_ip" {
  command = apply

  variables {
    restrict_to_my_ip = true
  }

  assert {
    condition     = keys(aws_vpc_security_group_ingress_rule.app_http) == ["203.0.113.7/32"]
    error_message = "With restrict_to_my_ip only the detected IP may connect."
  }
}

run "custom_allowed_cidrs" {
  command = apply

  variables {
    allowed_cidrs = ["198.51.100.0/24", "192.0.2.0/24"]
  }

  assert {
    condition     = length(aws_vpc_security_group_ingress_rule.app_http) == 2 && contains(keys(aws_vpc_security_group_ingress_rule.app_http), "198.51.100.0/24")
    error_message = "Every listed CIDR should get an ingress rule."
  }
}

run "graviton_instance_gets_arm_ami" {
  command = apply

  variables {
    instance_type = "t4g.small"
  }

  assert {
    condition     = data.aws_ssm_parameter.ami.name == "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"
    error_message = "A Graviton instance type needs the arm64 AMI."
  }
}

run "x86_instances_are_not_mistaken_for_graviton" {
  command = apply

  variables {
    instance_type = "m5.large"
  }

  assert {
    condition     = data.aws_ssm_parameter.ami.name == "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
    error_message = "m5 is x86_64."
  }
}

run "custom_image_is_used" {
  command = apply

  variables {
    app_image = "ghcr.io/someone/fork:1.2.3"
  }

  assert {
    condition     = strcontains(aws_instance.app.user_data, "docker pull \"ghcr.io/someone/fork:1.2.3\"")
    error_message = "app_image must reach the instance."
  }
}
