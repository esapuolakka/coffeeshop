terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
    http = {
      source  = "hashicorp/http"
      version = "~> 3.4"
    }
  }
}

provider "aws" {
  region = var.region

  # Only static values here. A default tag that is unknown at plan time (for
  # example anything derived from random_id) makes the AWS provider fail with
  # "inconsistent final plan ... tags_all". Per-resource Name tags are set on the
  # resources themselves.
  default_tags {
    tags = {
      Project   = "coffeeshop"
      ManagedBy = "terraform"
    }
  }
}
