data "aws_caller_identity" "current" {}

data "aws_availability_zones" "available" {
  state = "available"
}

# Not every instance type is offered in every availability zone.
data "aws_ec2_instance_type_offerings" "app" {
  filter {
    name   = "instance-type"
    values = [var.instance_type]
  }
  location_type = "availability-zone"
}

# Only used when restrict_to_my_ip is set.
data "http" "my_ip" {
  count = var.restrict_to_my_ip ? 1 : 0
  url   = "https://checkip.amazonaws.com"
}

resource "random_id" "suffix" {
  byte_length = 3
}

locals {
  name = "${var.name_prefix}-${random_id.suffix.hex}"

  # Public subnet (the web server) goes where the instance type exists,
  # RDS needs a subnet group that spans two zones.
  app_az = sort(data.aws_ec2_instance_type_offerings.app.locations)[0]
  db_azs = slice(sort(data.aws_availability_zones.available.names), 0, 2)

  allowed_cidrs = var.restrict_to_my_ip ? ["${chomp(data.http.my_ip[0].response_body)}/32"] : var.allowed_cidrs

  # Graviton instance types have a "g" right after the generation number (t4g, m7g, c6gd, ...)
  is_arm = can(regex("^[a-z]+[0-9]+g", var.instance_type))

  secret_path = "/${local.name}"
}
