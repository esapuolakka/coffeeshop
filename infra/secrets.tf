# Generated credentials. They are stored in SSM Parameter Store (SecureString);
# the instance reads them at boot through its IAM role, so no secret is ever
# written into the instance's user data. They also end up in the Terraform state,
# which is why the state file must stay private (it is git-ignored).

resource "random_password" "db" {
  length  = 32
  special = false
}

resource "random_password" "admin" {
  length  = 24
  special = false
}

resource "random_password" "remember_me" {
  length  = 48
  special = false
}

resource "aws_ssm_parameter" "db_password" {
  name  = "${local.secret_path}/db_password"
  type  = "SecureString"
  value = random_password.db.result
}

resource "aws_ssm_parameter" "admin_password" {
  name  = "${local.secret_path}/admin_password"
  type  = "SecureString"
  value = random_password.admin.result
}

resource "aws_ssm_parameter" "remember_me_key" {
  name  = "${local.secret_path}/remember_me_key"
  type  = "SecureString"
  value = random_password.remember_me.result
}
