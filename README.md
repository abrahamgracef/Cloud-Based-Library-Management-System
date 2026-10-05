# LibraCore - Modern SaaS Library Management System

A modern, full-featured Library Management System built with **Java 21**, **Spring Boot 3.2**, **Spring Data JPA**, **PostgreSQL**, and an interactive **Chart.js** analytics dashboard.

---

## Key Features

- **Book Catalog Management**: Complete inventory management with categorization, ISBN validation, and real-time stock tracking.
- **Member Directory**: Role-based access for librarians, faculty, and students with unique member identification codes.
- **Borrowing & Return Workflows**: Book issuing with configurable loan durations and automatic availability adjustments upon return.
- **Automated Fine Calculation**: Dynamic penalty calculation for overdue returns with payment reconciliation.
- **Data Analytics & Reports**: Circulation metrics, category distribution, and borrowing activity powered by Chart.js.
- **Dual User Experience**: Dedicated interfaces for Librarian Administration and Student Book Discovery.
- **API Documentation**: Built-in interactive OpenAPI 3.0 / Swagger documentation.

---

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.3, Spring Data JPA, Spring Validation, Lombok
- **Database**: PostgreSQL 15 (Production & Docker), H2 In-Memory Database (Development & Testing)
- **Frontend**: Responsive Single Page Application (HTML5, CSS3, JavaScript, Chart.js, Font Awesome)
- **API Documentation**: OpenAPI 3.0, Swagger UI (`/swagger-ui.html`)
- **Containerization**: Docker, Docker Compose (Multi-stage build)
- **Testing**: JUnit 5, Mockito, Spring Boot Test (34/34 passing test suites)

---

## Getting Started

### Prerequisites
- JDK 21 installed (`java -version`)
- Apache Maven 3.9+ installed (`mvn -version`)
- Docker & Docker Compose (Optional)

### Running Locally with Maven
```bash
# Clone the repository
git clone https://github.com/abrahamgracef/Cloud-Based-Library-Management-System.git
cd Cloud-Based-Library-Management-System

# Run the Spring Boot application
mvn spring-boot:run
```

Access the application in your browser:
- **Web Application**: `http://localhost:8085/`
- **Swagger API Docs**: `http://localhost:8085/swagger-ui.html`
- **H2 Console**: `http://localhost:8085/h2-console`

### Running with Docker Compose
```bash
# Build and run containers in detached mode
docker-compose up -d --build

# View application logs
docker-compose logs -f app

# Stop containers
docker-compose down
```

---

## REST API Reference

| HTTP Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/books` | Retrieve all books in catalog |
| `GET` | `/api/books/{id}` | Retrieve book by ID |
| `GET` | `/api/books/search?query={q}` | Search books by title, author, category, or ISBN |
| `POST` | `/api/books` | Add new book to catalog |
| `PUT` | `/api/books/{id}` | Update book details |
| `DELETE` | `/api/books/{id}` | Remove book from catalog |
| `GET` | `/api/members` | Retrieve registered members |
| `POST` | `/api/members` | Register new library member |
| `POST` | `/api/borrowing/issue` | Issue book to a member |
| `POST` | `/api/borrowing/return/{id}` | Process book return |
| `GET` | `/api/borrowing` | Retrieve all borrowing records |
| `GET` | `/api/fines` | Retrieve library fine records |
| `POST` | `/api/fines/{id}/pay` | Mark fine as paid |
| `GET` | `/api/stats` | Dashboard statistics summary |

---

## Running Test Suites

```bash
mvn clean test
```

Includes unit and integration tests for services, business logic, validation rules, and REST controllers.

---

## License
This project is open-source and available under the [MIT License](LICENSE).
