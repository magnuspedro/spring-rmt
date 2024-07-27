provider "aws" {

  access_key                  = "mock_access_key"
  secret_key                  = "mock_secret_key"
  region                      = "sa-east-1"
  s3_use_path_style           = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    s3 = "http://localstack:4566"
  }

}

resource "aws_s3_bucket" "refactored-projects_bucket" {
  bucket = "refactored-projects"
}

resource "aws_s3_bucket" "projects_bucket" {
  bucket = "projects"
}

# resource "aws_elasticache_cluster" "projects_redis" {
#   cluster_id           = "projectsredis"
#   engine               = "redis"
#   node_type            = "t4g.micro"
#   num_cache_nodes      = 1
#   parameter_group_name = "default.redis7.2"
#   engine_version       = "7.2"
#   port                 = 6379
# }

# resource "aws_sqs_queue" "detection_service" {
#   name                      = "detect-pattern"
#   delay_seconds             = 0
#   max_message_size          = 2048
#   message_retention_seconds = 86400
#   receive_wait_time_seconds = 10
#   redrive_policy = jsonencode({
#     deadLetterTargetArn = aws_sqs_queue.detection_service_deadletter.arn
#     maxReceiveCount     = 4
#   })
# }
#
# resource "aws_sqs_queue" "detection_service_deadletter" {
#   name = "detect-pattern-deadletter"
# }
#
#
# resource "aws_sqs_queue" "metrics_service" {
#   name                      = "measure-pattern"
#   delay_seconds             = 0
#   max_message_size          = 2048
#   message_retention_seconds = 86400
#   receive_wait_time_seconds = 10
#   redrive_policy = jsonencode({
#     deadLetterTargetArn = aws_sqs_queue.metrics_service_deadletter.arn
#     maxReceiveCount     = 4
#   })
# }
#
# resource "aws_sqs_queue" "metrics_service_deadletter" {
#   name = "measure-pattern-deadletter"
# }
