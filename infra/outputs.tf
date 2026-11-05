output "app_url" {
  description = "Where the shop is reachable (plain HTTP). It can take a minute or two after apply until the app has started."
  value       = "http://${aws_eip.app.public_ip}"
}

output "health_url" {
  description = "Health check endpoint, handy for waiting until the app is up."
  value       = "http://${aws_eip.app.public_ip}/actuator/health"
}

output "admin_username" {
  value = var.admin_username
}

output "admin_password" {
  description = "Generated admin password. Show it with: terraform output -raw admin_password"
  value       = random_password.admin.result
  sensitive   = true
}

output "image_bucket" {
  description = "S3 bucket holding the uploaded product images."
  value       = aws_s3_bucket.images.id
}

output "db_endpoint" {
  description = "RDS endpoint (private, reachable only from the web server)."
  value       = aws_db_instance.main.address
}

output "instance_id" {
  value = aws_instance.app.id
}

output "shell_command" {
  description = "Open a shell on the server (no SSH key needed; requires the AWS CLI and Session Manager plugin)."
  value       = "aws ssm start-session --region ${var.region} --target ${aws_instance.app.id}"
}
