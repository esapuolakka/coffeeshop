variable "region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "eu-north-1"
}

variable "name_prefix" {
  description = "Prefix for resource names. A random suffix is added, so several deployments can live in one account."
  type        = string
  default     = "coffeeshop"
}

variable "app_image" {
  description = "Container image to run. Pin a version tag for reproducible deployments. Must be pullable without credentials (a public registry) and match the instance CPU architecture (the published image is multi-arch)."
  type        = string
  default     = "ghcr.io/esapuolakka/coffeeshop-java:latest"
}

variable "instance_type" {
  description = "EC2 instance type. Graviton types (t4g, m7g, ...) work too: the AMI architecture follows the type."
  type        = string
  default     = "t3.small"
}

variable "db_instance_class" {
  description = "RDS instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS storage in GiB."
  type        = number
  default     = 20
}

variable "db_engine_version" {
  description = "PostgreSQL major (or major.minor) version."
  type        = string
  default     = "15"
}

variable "allowed_cidrs" {
  description = "CIDR blocks allowed to reach the web app on port 80."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "restrict_to_my_ip" {
  description = "If true, only your current public IP (detected when Terraform runs) may reach the app. Overrides allowed_cidrs."
  type        = bool
  default     = false
}

variable "vpc_cidr" {
  description = "CIDR block of the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "admin_username" {
  description = "Username of the application's admin account. The password is generated."
  type        = string
  default     = "admin"
}
