resource "aws_s3_bucket" "refactored_projects_bucket" {
  bucket = "refactored-projects"
}

resource "aws_s3_bucket" "projects_bucket" {
  bucket = "projects"
}

# Lifecycle configurations disabled for LocalStack compatibility
# Uncomment for production deployment
# resource "aws_s3_bucket_lifecycle_configuration" "projects_bucket_lifecycle" {
#   bucket = aws_s3_bucket.projects_bucket.id
#
#   rule {
#     id     = "projects-expire-after-one-day"
#     status = "Enabled"
#
#     expiration {
#       days = 1
#     }
#
#     filter {}
#   }
# }
#
# resource "aws_s3_bucket_lifecycle_configuration" "refactored_projects_bucket_lifecycle" {
#   bucket = aws_s3_bucket.refactored_projects_bucket.id
#
#   rule {
#     id     = "refactored-projects-expire-after-one-day"
#     status = "Enabled"
#
#     expiration {
#       days = 1
#     }
#
#     filter {}
#   }
# }

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
