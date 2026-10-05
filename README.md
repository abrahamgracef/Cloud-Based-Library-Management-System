# Cloud-Based Library Management System Using DevOps and AWS

## Student & Project Information
- **Student Name**: Abraham Grace F
- **Register Number**: 24MIS0211
- **Course Code**: ISWE406L
- **Project Stage**: Review 1 Project

---

## Executive Summary & Problem Statement

### Problem Statement
Traditional library management systems suffer from high maintenance costs, scalability limitations, manual record-keeping, slow search performance, lack of automated infrastructure provisioning, and inefficient deployment workflows. Without automated testing, containerization, and Continuous Integration / Continuous Deployment (CI/CD) pipelines, deploying updates results in significant system downtime and potential data inconsistencies.

### Proposed Solution
This project introduces a modern, cloud-native **Library Management System** built with **Java 21**, **Spring Boot 3.2**, **Spring Data JPA**, and **PostgreSQL / H2**. The application is containerized using **Docker** and **Docker Compose**, and deployed to **Amazon Web Services (AWS)** using automated DevOps pipelines powered by **AWS CodePipeline**, **CodeBuild**, **CodeDeploy**, **GitHub Actions**, **CloudFormation**, and **Terraform**. 

Key capabilities include automated book inventory management, member enrollment, borrowing and returning workflows, automated overdue fine calculation, book cover upload integration via Amazon S3, real-time alert notifications via Amazon SNS, centralized logging via CloudWatch, and full security auditing via CloudTrail and IAM.

---

## Technology Stack

- **Core Application Backend**: Java 21, Spring Boot 3.2.3, Spring Data JPA, Spring Validation, Lombok
- **Database**: PostgreSQL 15 (Production & Docker), H2 In-Memory Database (Development & Testing)
- **Frontend / UI**: HTML5, CSS3, JavaScript (Single Page UI placed under `src/main/resources/static/`)
- **API Documentation**: OpenAPI 3.0, Swagger UI (`http://localhost:8080/swagger-ui.html`)
- **Containerization**: Docker, Docker Compose (Multi-stage builds)
- **AWS Infrastructure & Cloud Services**:
  - **AWS EC2 (t3.micro)**: Application server hosting Spring Boot application runtime
  - **AWS RDS PostgreSQL (db.t3.micro)**: Managed relational database instance
  - **AWS S3**: Cloud object storage for book cover image uploads
  - **AWS SNS**: Topic-based notification system for overdue book alerts and notifications
  - **AWS CloudWatch & CloudTrail**: Application logs aggregation, performance metrics, and audit tracking
  - **AWS IAM**: Fine-grained role-based security policies and EC2 instance profiles
- **DevOps & Infrastructure as Code (IaC)**:
  - **AWS CodeBuild**: Automated build execution and artifact packaging (`buildspec.yml`)
  - **AWS CodeDeploy**: Automated application deployment and lifecycle hook execution (`appspec.yml`)
  - **AWS CodePipeline**: End-to-end cloud CI/CD orchestration
  - **GitHub Actions**: Continuous integration, automated test runner, and ECR docker image registry pusher
  - **CloudFormation & Terraform**: Infrastructure as Code templates for automated AWS stack provisioning

---

## DevOps Workflow & AWS Architecture

```text
                                  +-------------------+
                                  |   Developer Git   |
                                  |  Push to GitHub   |
                                  +---------+---------+
                                            |
                                            v
                                 +----------+----------+
                                 |  GitHub Actions /   |
                                 |   AWS CodePipeline  |
                                 +----------+----------+
                                            |
                                            v
                                 +----------+----------+
                                 |   AWS CodeBuild     |
                                 |  (mvn test & jar)   |
                                 +----------+----------+
                                            |
                                            v
                                 +----------+----------+
                                 |   AWS CodeDeploy    |
                                 +----------+----------+
                                            |
                                            v
+-----------------------------------------------------------------------------------+
| AWS Cloud Infrastructure (VPC: 10.0.0.0/16)                                        |
|                                                                                   |
|  +--------------------------------+       +------------------------------------+  |
|  | Public Subnet (10.0.1.0/24)     |       | Private Subnets (10.0.2.0 & 3.0)   |  |
|  |                                |       |                                    |  |
|  |  +--------------------------+  |       |  +------------------------------+  |  |
|  |  | EC2 Instance (t3.micro)  |  |       |  | RDS PostgreSQL (db.t3.micro)|  |  |
|  |  | - Java 21 Runtime        +--+-------+->| - Database: librarydb        |  |  |
|  |  | - CodeDeploy Agent       |  |Port   |  | - Port: 5432                 |  |  |
|  |  | - Spring Boot Port 8080  |  |5432   |  +------------------------------+  |  |
|  |  +------------+-------------+  |       |                                    |  |
|  +---------------+----------------+       +------------------------------------+  |
|                  |                                                                |
|                  +------------------+------------------+                          |
|                  |                  |                  |                          |
|                  v                  v                  v                          |
|           +--------------+   +--------------+   +-------------------+             |
|           | AWS S3 Bucket|   |  AWS SNS     |   | AWS CloudWatch    |             |
|           | (Book Covers)|   | (Alerts Topic|   | (Logs & Metrics)  |             |
|           +--------------+   +--------------+   +-------------------+             |
+-----------------------------------------------------------------------------------+
```

---

## Local Setup & Execution Guide

### Prerequisites
- JDK 21 installed (`java -version`)
- Apache Maven 3.9+ installed (`mvn -version`)
- Docker & Docker Compose installed (optional for container execution)

### Option 1: Running with Maven (In-Memory H2 Database)
```bash
# Clone the repository and navigate to project root
cd LibraryManagementSystem

# Build and run the Spring Boot application
mvn spring-boot:run
```
The application will launch locally at:
- Web User Interface: `http://localhost:8080/`
- Swagger UI API Documentation: `http://localhost:8080/swagger-ui.html`

### Option 2: Running with Docker Compose (Spring Boot + PostgreSQL 15)
```bash
# Build Docker image and launch containers in detached mode
docker-compose up -d --build

# View container logs
docker-compose logs -f app

# Stop containers
docker-compose down
```

---

## AWS Deployment Guide

### Deployment via AWS CloudFormation / Terraform
1. **CloudFormation**:
   - Navigate to AWS CloudFormation console -> Create Stack.
   - Upload `aws/cloudformation.yml`.
   - Specify stack parameter values and deploy.

2. **Terraform**:
   ```bash
   cd aws/terraform
   terraform init
   terraform plan
   terraform apply -auto-approve
   ```

### Automated CodePipeline & CodeDeploy Setup
1. AWS CodePipeline automatically fetches code from GitHub.
2. CodeBuild uses `buildspec.yml` to run `mvn test` and compile `library-management-system-1.0.0-SNAPSHOT.jar`.
3. CodeDeploy uses `appspec.yml` to run deployment lifecycle scripts:
   - `scripts/stop_server.sh` (Stops current running service)
   - `scripts/install_dependencies.sh` (Installs Corretto 21 & sets up directory `/opt/library-app`)
   - `scripts/start_server.sh` (Starts background process with log redirect to `/var/log/library-app.log`)
   - `scripts/validate_service.sh` (Polls health check endpoint `http://localhost:8080/api/stats`)

---

## REST API Documentation

| HTTP Method | Endpoint Path | Description |
|---|---|---|
| `GET` | `/api/books` | Get all books in catalog |
| `GET` | `/api/books/{id}` | Get book by unique ID |
| `GET` | `/api/books/isbn/{isbn}` | Get book by ISBN |
| `GET` | `/api/books/search?query={q}` | Search books by title, author, category, or ISBN |
| `POST` | `/api/books` | Create a new book entry |
| `PUT` | `/api/books/{id}` | Update existing book details |
| `DELETE` | `/api/books/{id}` | Delete book entry |
| `POST` | `/api/borrowing/issue` | Issue a book to a active member |
| `POST` | `/api/borrowing/return/{id}` | Return a book and auto-calculate overdue fines |
| `GET` | `/api/borrowing/history/member/{id}` | Get borrowing history for a member |
| `GET` | `/api/borrowing/active` | Get active issued books |
| `GET` | `/api/borrowing/overdue` | Get overdue borrow records |
| `GET` | `/api/borrowing` | Get all borrow records |
| `GET` | `/api/stats` | Dashboard statistics overview |

Interactive OpenAPI documentation is available via Swagger UI at `http://localhost:8080/swagger-ui.html`.

---

## Testing Guide

The project includes comprehensive Unit & MockMvc Integration tests covering services and controllers.

```bash
# Execute unit & integration test suites
mvn test
```

### Test Coverage Summary
- `BookServiceTest.java`: Unit tests using JUnit 5 & Mockito for book creation, validation, search, updating, deletion, and duplicate ISBN handling.
- `BorrowingServiceTest.java`: Unit tests for issuing books, member validation, stock checking, return processing, and overdue fine calculation ($1/day).
- `BookControllerTest.java`: Integration tests using MockMvc for REST endpoints, request validation, and HTTP response status codes.
- `BorrowingControllerTest.java`: Integration tests using MockMvc for borrowing and return REST APIs.
