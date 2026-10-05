terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "environment" {
  type    = string
  default = "prod"
}

# VPC & Networking
resource "aws_vpc" "library_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name        = "library-management-vpc"
    Environment = var.environment
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.library_vpc.id

  tags = {
    Name = "library-management-igw"
  }
}

resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.library_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "${var.aws_region}a"

  tags = {
    Name = "library-public-subnet"
  }
}

resource "aws_subnet" "private_subnet_1" {
  vpc_id            = aws_vpc.library_vpc.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "library-private-subnet-1"
  }
}

resource "aws_subnet" "private_subnet_2" {
  vpc_id            = aws_vpc.library_vpc.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "library-private-subnet-2"
  }
}

resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.library_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "library-public-route-table"
  }
}

resource "aws_route_table_association" "public_assoc" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_rt.id
}

# Security Groups
resource "aws_security_group" "ec2_sg" {
  name        = "library-ec2-sg"
  description = "Allow HTTP on 8080 and SSH on 22"
  vpc_id      = aws_vpc.library_vpc.id

  ingress {
    description = "HTTP Spring Boot Port"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH Access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "library-ec2-sg"
  }
}

resource "aws_security_group" "rds_sg" {
  name        = "library-rds-sg"
  description = "Allow PostgreSQL access from EC2"
  vpc_id      = aws_vpc.library_vpc.id

  ingress {
    description     = "PostgreSQL from EC2 SG"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "library-rds-sg"
  }
}

# Database Subnet Group & RDS PostgreSQL Instance
resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "library-db-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_1.id, aws_subnet.private_subnet_2.id]

  tags = {
    Name = "library-db-subnet-group"
  }
}

resource "aws_db_instance" "postgres" {
  identifier             = "library-postgres-db"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  db_name                = "librarydb"
  username               = "libraryuser"
  password               = "LibraryPassword123!"
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  skip_final_snapshot    = true

  tags = {
    Name = "library-postgres-rds"
  }
}

# S3 Bucket for Book Covers Storage
resource "aws_s3_bucket" "book_covers" {
  bucket        = "library-management-book-covers-bucket"
  force_destroy = true

  tags = {
    Name = "library-management-book-covers"
  }
}

# SNS Topic for Notifications
resource "aws_sns_topic" "notifications" {
  name = "library-management-notifications-topic"
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "app_logs" {
  name              = "/aws/library-management-system/application"
  retention_in_days = 30
}

# IAM Role & Instance Profile for EC2 / CodeDeploy / CloudWatch
resource "aws_iam_role" "ec2_role" {
  name = "library-ec2-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "s3_read" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "cloudwatch_agent" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_role_policy_attachment" "codedeploy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSCodeDeployFullAccess"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "library-ec2-instance-profile"
  role = aws_iam_role.ec2_role.name
}

# EC2 Instance (t3.micro)
resource "aws_instance" "app_server" {
  ami                  = "ami-0c101f26f147fa7fd" # Amazon Linux 2023 AMI
  instance_type        = "t3.micro"
  subnet_id            = aws_subnet.public_subnet.id
  vpc_security_group_ids = [aws_security_group.ec2_sg.id]
  iam_instance_profile = aws_iam_instance_profile.ec2_profile.name

  user_data = <<-EOF
              #!/bin/bash
              yum update -y
              yum install -y ruby wget java-21-amazon-corretto-devel
              cd /home/ec2-user
              wget https://aws-codedeploy-${var.aws_region}.s3.${var.aws_region}.amazonaws.com/latest/install
              chmod +x ./install
              ./install auto
              systemctl enable codedeploy-agent
              systemctl start codedeploy-agent
              EOF

  tags = {
    Name        = "library-management-app-server"
    Environment = var.environment
  }
}

outputs {
  ec2_public_ip = aws_instance.app_server.public_ip
  rds_endpoint  = aws_db_instance.postgres.endpoint
  s3_bucket     = aws_s3_bucket.book_covers.bucket
  sns_topic_arn = aws_sns_topic.notifications.arn
}
