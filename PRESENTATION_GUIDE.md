# Review 1 Viva & Presentation Guide
## Cloud-Based Library Management System Using DevOps and AWS
- **Student Name**: Abraham Grace F
- **Register Number**: 24MIS0211
- **Course Code**: ISWE406L
- **Live AWS URL**: [http://52.66.211.1:8085](http://52.66.211.1:8085)
- **GitHub Repository**: [https://github.com/abrahamgracef/Cloud-Based-Library-Management-System](https://github.com/abrahamgracef/Cloud-Based-Library-Management-System)

---

## 1. 2-Minute Presentation Pitch

> "Good morning/afternoon, Sir. My project is a **Cloud-Based Library Management System Using DevOps and AWS**. 
> 
> The application is built using **Java 21 and Spring Boot 3.2** with a responsive single-page frontend. It automates essential library operations including book catalog management, member registration, book issuing, returns, and automatic fine calculation.
> 
> Beyond application features, the primary focus of this project is implementing an **end-to-end automated DevOps workflow on AWS**.
> 
> Whenever new code is pushed to our **GitHub** repository, our CI/CD pipeline triggers automatically:
> 1. **AWS CodeBuild** compiles the Spring Boot source code and executes all **34 unit and integration tests** using Maven and JUnit.
> 2. Upon successful testing, **AWS CodeDeploy** deploys the application smoothly onto an **Amazon EC2** instance (`t3.micro`) running in our custom VPC.
> 3. Structured application data (books, members, borrow records, fines) is persisted in **Amazon RDS PostgreSQL 15**.
> 4. Heavy binary files like book cover images are offloaded to **Amazon S3** for scalable object storage.
> 5. **Amazon CloudWatch** monitors server health, CPU, memory, and logs in real time.
> 6. **AWS CloudTrail** maintains complete audit logs of all AWS API actions.
> 7. **Amazon SNS** sends instant alerts for overdue book returns or deployment failures.
> 8. **AWS IAM** enforces strict least-privilege security roles across all services.
> 
> The application is currently live and operational on AWS in the Mumbai region (`ap-south-1`)."

---

## 2. Walkthrough of the 11 Workflow Components

```text
Developer ──► GitHub ──► CodePipeline ──► CodeBuild ──► CodeDeploy ──► Amazon EC2
                                                                           │
                             ┌─────────────────────────────────────────────┴─────────────┐
                             ▼                                                           ▼
                      Amazon S3 (Files)                                       Amazon RDS PostgreSQL (Data)

            [ Monitoring & Security Layers: CloudWatch • CloudTrail • SNS • IAM ]
```

| # | AWS / DevOps Component | Exact Role in Project |
|---|---|---|
| **1** | **Developer &rarr; GitHub** | Developer writes code and pushes commits to the `main` branch on GitHub. |
| **2** | **GitHub &rarr; AWS CodePipeline** | CodePipeline listens to GitHub events via webhooks and automatically triggers the CI/CD pipeline. |
| **3** | **AWS CodeBuild** | CodeBuild launches a clean container, compiles Java 21, and executes all **34 JUnit tests**. If tests fail, the build fails and alerts are sent. |
| **4** | **AWS CodeDeploy** | CodeDeploy deploys the verified JAR onto EC2 using in-place deployment with systemd lifecycle hooks (`stop_server`, `install_dependencies`, `start_server`, `validate_service`). |
| **5** | **Amazon EC2** | `t3.micro` instance running Amazon Linux 2023 that hosts the live Spring Boot application on Port 8085. |
| **6** | **Amazon S3** | S3 bucket (`library-management-book-covers-232901749713`) storing book cover images separately from the database. |
| **7** | **Amazon RDS PostgreSQL** | Managed PostgreSQL 15 database instance (`db.t3.micro`) residing in private subnets for high security and relational integrity. |
| **8** | **Amazon CloudWatch** | Aggregates application log streams (`/aws/library-management-system/application`) and tracks CPU, memory, and uptime metrics. |
| **9** | **AWS CloudTrail** | Tracks user and API activity across the AWS account for compliance and security auditing. |
| **10** | **Amazon SNS** | Notification topic (`library-management-notifications`) sending alerts for overdue book fines and pipeline alerts. |
| **11** | **AWS IAM** | Role-based access control (`EC2CodeDeployRole`, instance profiles) granting least-privilege permissions. |

---

## 3. Potential Questions & Answers

### Q1: Why do we need this DevOps workflow?
**Answer:** "This DevOps workflow eliminates manual server maintenance and risky manual deployments. It guarantees that any code pushed to GitHub is automatically tested, built, and deployed to production with zero human intervention. If any test fails, CodeBuild stops the deployment, preventing bugs from reaching users."

### Q2: What is the difference between Amazon CloudWatch and AWS CloudTrail?
**Answer:** 
* **CloudWatch** monitors **system performance and operational health** (*"What is happening with the servers and logs?"*).
* **CloudTrail** audits **API calls and user actions** (*"Who made what changes in the AWS account and when?"*).

### Q3: Why separate storage between Amazon S3 and Amazon RDS?
**Answer:** "Relational databases like PostgreSQL are optimized for structured tabular queries (indexing, foreign keys, ACID transactions), whereas storing large image files directly in the database causes table bloat, slow backups, and high database I/O. Storing images in S3 offloads heavy binary traffic to a dedicated object store."

### Q4: How is high availability and zero downtime achieved during deployment?
**Answer:** "We use automated deployment scripts managed by CodeDeploy with validation hooks. CodeDeploy can perform Blue/Green or rolling in-place deployments, verifying health checks (`/api/stats`) before routing traffic."

### Q5: How do you ensure the deployment stays within AWS Free Tier limits?
**Answer:** "We use `t3.micro` for EC2 (750 free hours/month), `db.t3.micro` Single-AZ for RDS (750 free hours/month), 5 GB of standard S3 storage, and standard CloudWatch metrics, keeping total operational costs at $0.00."
