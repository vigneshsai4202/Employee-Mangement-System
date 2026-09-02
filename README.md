# Employee Management System

A production-style REST API built with Java 17, Spring Boot 3, and MySQL. Demonstrates layered architecture, REST design, JPA/Hibernate, Bean Validation, global exception handling, unit testing, and Dockerized deployment with Docker Compose.

---

## Architecture

```mermaid
flowchart TD
    Client(["Client (Postman / Browser)"])
    Controller["Controller Layer\n@RestController"]
    Service["Service Layer\n@Service + @Transactional"]
    Repository["Repository Layer\n@Repository + JpaRepository"]
    Hibernate["Hibernate ORM"]
    MySQL[("MySQL Database")]

    Client -->|"HTTP Request (JSON)"| Controller
    Controller -->|"EmployeeRequest DTO"| Service
    Service -->|"Entity"| Repository
    Repository -->|"JPQL / Derived Query"| Hibernate
    Hibernate -->|"SQL"| MySQL
    MySQL -->|"ResultSet"| Hibernate
    Hibernate -->|"Entity"| Repository
    Repository -->|"Entity"| Service
    Service -->|"EmployeeResponse DTO"| Controller
    Controller -->|"HTTP Response (JSON)"| Client
```

### Request Lifecycle — `POST /api/employees`

```
1. Client sends HTTP POST with JSON body
2. DispatcherServlet routes to EmployeeController.createEmployee()
3. @Valid triggers Hibernate Validator on EmployeeRequest
   └─ If invalid → MethodArgumentNotValidException → GlobalExceptionHandler → 400
4. Controller calls employeeService.createEmployee(request)
5. Service checks for duplicate email via repository.existsByEmail()
   └─ If duplicate → DuplicateEmailException → GlobalExceptionHandler → 409
6. Service calls mapper.toEntity(request) → Employee entity
7. Service calls repository.save(employee) → Hibernate issues INSERT
8. MySQL assigns AUTO_INCREMENT id, sets created_at and updated_at
9. Service calls mapper.toResponse(savedEmployee) → EmployeeResponse
10. Controller returns ResponseEntity with 201 CREATED + EmployeeResponse JSON
```

### Request Lifecycle — `GET /api/employees/{id}`

```
1. Client sends HTTP GET /api/employees/5
2. DispatcherServlet routes to EmployeeController.getEmployeeById(5)
3. Controller calls employeeService.getEmployeeById(5)
4. Service calls repository.findById(5) → Optional<Employee>
   └─ If empty → EmployeeNotFoundException → GlobalExceptionHandler → 404
5. Service calls mapper.toResponse(employee) → EmployeeResponse
6. Controller returns ResponseEntity with 200 OK + EmployeeResponse JSON
```

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Web | Spring MVC |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | MySQL 8 |
| Build Tool | Maven |
| Validation | Hibernate Validator (Bean Validation 3.0) |
| Boilerplate | Lombok |
| Testing | JUnit 5 + Mockito + MockMvc |
| Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Connection Pool | HikariCP |
| Containerization | Docker + Docker Compose |

---

## Project Structure

```
src/main/java/com/example/employeemanagement/
│
├── config/
│   └── OpenApiConfig.java          # Swagger/OpenAPI configuration
│
├── controller/
│   └── EmployeeController.java     # REST endpoints, HTTP layer only
│
├── service/
│   ├── EmployeeService.java        # Business contract (interface)
│   └── EmployeeServiceImpl.java    # Business logic implementation
│
├── repository/
│   └── EmployeeRepository.java     # Data access, derived + custom queries
│
├── entity/
│   └── Employee.java               # JPA entity, maps to employees table
│
├── dto/
│   ├── EmployeeRequest.java        # Inbound DTO with validation annotations
│   └── EmployeeResponse.java       # Outbound DTO for API responses
│
├── exception/
│   ├── EmployeeNotFoundException.java   # 404 custom exception
│   ├── DuplicateEmailException.java     # 409 custom exception
│   ├── GlobalExceptionHandler.java      # @RestControllerAdvice
│   └── ErrorResponse.java              # Structured error response shape
│
├── mapper/
│   └── EmployeeMapper.java         # Converts between Request, Entity, Response
│
└── EmployeeManagementApplication.java  # Entry point
```

---

## Database Schema

```sql
CREATE TABLE employees (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    first_name   VARCHAR(100)    NOT NULL,
    last_name    VARCHAR(100)    NOT NULL,
    email        VARCHAR(255)    NOT NULL,
    phone_number VARCHAR(20),
    department   VARCHAR(100)    NOT NULL,
    job_title    VARCHAR(100)    NOT NULL,
    salary       DECIMAL(15, 2),
    hire_date    DATE,
    created_at   DATETIME(6)     NOT NULL,
    updated_at   DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_employee_email UNIQUE (email)
);
```

### Design Decisions

- `BIGINT` for `id` — supports billions of rows, never use `INT` for primary keys
- `DECIMAL(15,2)` for `salary` — exact decimal arithmetic, never use `FLOAT`/`DOUBLE` for money
- `DATE` for `hire_date` — date only, no time component needed
- `DATETIME(6)` for timestamps — microsecond precision audit fields
- Named unique constraint `uk_employee_email` — better error messages than unnamed constraints

### Future Schema Expansion

```mermaid
erDiagram
    EMPLOYEE {
        bigint id PK
        string first_name
        string last_name
        string email
        bigint department_id FK
        bigint manager_id FK
    }
    DEPARTMENT {
        bigint id PK
        string name
        string location
    }
    PROJECT {
        bigint id PK
        string name
        date start_date
    }
    EMPLOYEE_PROJECT {
        bigint employee_id FK
        bigint project_id FK
        string role
    }
    EMPLOYEE }o--|| DEPARTMENT : "belongs to"
    EMPLOYEE }o--o| EMPLOYEE : "managed by"
    EMPLOYEE ||--o{ EMPLOYEE_PROJECT : "assigned to"
    PROJECT ||--o{ EMPLOYEE_PROJECT : "has"
```

---

## API Endpoints

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/employees` | Create a new employee | 201, 400, 409 |
| `GET` | `/api/employees` | Get all employees | 200 |
| `GET` | `/api/employees/{id}` | Get employee by ID | 200, 404 |
| `PUT` | `/api/employees/{id}` | Update employee | 200, 400, 404, 409 |
| `DELETE` | `/api/employees/{id}` | Delete employee | 204, 404 |
| `GET` | `/api/employees/search?keyword=` | Search by name or email | 200 |
| `GET` | `/api/employees/department/{dept}` | Filter by department | 200 |

---

## Request / Response Examples

### Create Employee

**Request**
```http
POST /api/employees
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "9876543210",
  "department": "Engineering",
  "jobTitle": "Software Engineer",
  "salary": 75000.00,
  "hireDate": "2024-01-15"
}
```

**Response — 201 Created**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "9876543210",
  "department": "Engineering",
  "jobTitle": "Software Engineer",
  "salary": 75000.00,
  "hireDate": "2024-01-15",
  "createdAt": "2024-08-14T17:30:00",
  "updatedAt": "2024-08-14T17:30:00"
}
```

### Validation Error

**Request**
```json
{
  "firstName": "",
  "email": "notanemail",
  "salary": -500
}
```

**Response — 400 Bad Request**
```json
{
  "timestamp": "2024-08-14T17:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request contains invalid fields",
  "path": "/api/employees",
  "fieldErrors": {
    "firstName": "First name is required",
    "email": "Email must be a valid email address",
    "salary": "Salary must be a positive value",
    "lastName": "Last name is required",
    "department": "Department is required",
    "jobTitle": "Job title is required",
    "hireDate": "Hire date is required"
  }
}
```

### Not Found Error

**Response — 404 Not Found**
```json
{
  "timestamp": "2024-08-14T17:30:00",
  "status": 404,
  "error": "Employee Not Found",
  "message": "Employee with ID 99 not found",
  "path": "/api/employees/99"
}
```

### Duplicate Email Error

**Response — 409 Conflict**
```json
{
  "timestamp": "2024-08-14T17:30:00",
  "status": 409,
  "error": "Duplicate Email",
  "message": "An employee with email 'john.smith@example.com' already exists",
  "path": "/api/employees"
}
```

---

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+
- Docker + Docker Compose (for containerized deployment)

### Setup (Traditional)

**1. Clone the repository**
```bash
git clone https://github.com/vigneshsai4202/Employee-Mangement-System.git
cd employee-management-system
```

**2. Create the database**
```sql
CREATE DATABASE employee_db;
```

**3. Configure credentials**

Open `src/main/resources/application.properties` and update:
```properties
spring.datasource.password=${DB_PASSWORD:your_password}
```

Or set environment variables (recommended):
```bash
set DB_URL=jdbc:mysql://localhost:3306/employee_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
set DB_USERNAME=root
set DB_PASSWORD=your_password
```

**4. Run the application**
```bash
mvn spring-boot:run
```

**5. Verify startup**

Look for:
```
Started EmployeeManagementApplication in X.XXX seconds
```

### Access Points

| URL | Description |
|---|---|
| `http://localhost:8080/api/employees` | REST API base URL |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI |
| `http://localhost:8080/api-docs` | Raw OpenAPI JSON |

---

## Docker Setup

### Build Docker Image

**Build the Docker image locally:**
```bash
docker build -t employee-management-system:latest .
```

**View the built image:**
```bash
docker images | grep employee-management-system
```

### Run with Docker Compose (Recommended)

**1. Start services with Docker Compose:**
```bash
docker-compose up -d
```

This command:
- Creates and starts both the MySQL database and Spring Boot application containers
- Automatically initializes the database schema
- Exposes the API on `http://localhost:8080`
- Mounts MySQL data in a named volume `employee_db_data` for persistence

**2. View running containers:**
```bash
docker-compose ps
```

**3. View application logs:**
```bash
docker-compose logs -f app
```

**4. View database logs:**
```bash
docker-compose logs -f db
```

**5. Access the application:**
- API: `http://localhost:8080/api/employees`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

**6. Stop all services:**
```bash
docker-compose down
```

**7. Stop services and remove volumes (reset everything):**
```bash
docker-compose down -v
```

### Run with Docker Only

**Run the application container (requires external MySQL):**
```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/employee_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=your_password \
  employee-management-system:latest
```

### Docker Compose Configuration

The `docker-compose.yml` includes:

**App Service:**
- Image: `employee-management-system:latest`
- Port: `8080:8080`
- Depends on: `db`
- Environment variables for database connection
- Automatic health check (every 10 seconds)

**Database Service:**
- Image: `mysql:8.0`
- Port: `3306:3306`
- Root password: `root_password` (change in production)
- Automatic database creation: `employee_db`
- Data persistence: Named volume `employee_db_data`

### Dockerfile Details

The `Dockerfile`:
- **Base Image:** `openjdk:17-jdk-slim` (lightweight, production-ready)
- **Build Stage:** Multi-stage build to keep image size small (~200MB)
- **Build Process:** Maven compiles and packages the application as a JAR
- **Runtime:** JAR runs as the main process in the container

### Docker Best Practices Used

- ✅ **Multi-stage build** — reduces final image size
- ✅ **Health checks** — enables Docker to monitor container health
- ✅ **Named volumes** — persists MySQL data across container restarts
- ✅ **Environment variables** — externalize configuration for different environments
- ✅ **Service dependencies** — ensures database starts before application
- ✅ **Logging** — application logs visible via `docker-compose logs`
- ✅ **Non-root user** (production recommendation) — improves security

### Troubleshooting Docker Issues

**Container won't start:**
```bash
docker-compose logs app
```

**Database connection refused:**
- Ensure `db` container is healthy: `docker-compose ps`
- Check database logs: `docker-compose logs db`
- Wait 10-15 seconds for MySQL to fully initialize on first run

**Port already in use:**
```bash
# Use different ports in docker-compose.yml or:
docker-compose -p unique_name up
```

**Clean up all Docker artifacts:**
```bash
docker-compose down -v
docker image rm employee-management-system:latest
```

---

## Running Tests

```bash
# Run all tests
mvn test

# Run only service tests
mvn test -Dtest=EmployeeServiceImplTest

# Run only controller tests
mvn test -Dtest=EmployeeControllerTest
```

Expected output:
```
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
```

---

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | Full JDBC connection URL | `jdbc:mysql://db:3306/employee_db...` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | *(must be set)* |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL strategy | `update` |
| `SERVER_PORT` | Spring Boot server port | `8080` |

---

## Exception Handling

All exceptions are handled centrally by `GlobalExceptionHandler` using `@RestControllerAdvice`.

| Exception | HTTP Status | Trigger |
|---|---|---|
| `EmployeeNotFoundException` | 404 Not Found | ID doesn't exist |
| `DuplicateEmailException` | 409 Conflict | Email already taken |
| `MethodArgumentNotValidException` | 400 Bad Request | Bean validation fails |
| `DataIntegrityViolationException` | 409 Conflict | DB constraint violated |
| `Exception` | 500 Internal Server Error | Unexpected error |

Stack traces are never exposed to API clients.

---

## Key Design Decisions

**Why DTOs instead of exposing entities?**
Entities are tied to the database schema. Exposing them directly couples your API contract to your DB structure, leaks internal fields, and prevents independent evolution of both layers.

**Why constructor injection instead of `@Autowired` on fields?**
Constructor injection makes dependencies explicit, enables `final` fields (immutability), and allows instantiation without Spring (critical for unit testing).

**Why `@Transactional(readOnly = true)` on read methods?**
Hibernate skips dirty checking for read-only transactions, improving performance. Some databases also apply read optimizations for read-only transactions.

**Why `existsByEmailAndIdNot` for updates?**
`existsByEmail` alone would block an employee from keeping their own email during an update. The `AndIdNot` clause excludes the current employee from the uniqueness check.

**Why Docker + Docker Compose?**
Containerization ensures consistent environments across development, testing, and production. Docker Compose orchestrates multi-container applications with a single command, eliminating "works on my machine" problems.

---

## Future Improvements

- [x] Docker + Docker Compose for containerized deployment
- [ ] Pagination and sorting on `GET /api/employees` using `Pageable`
- [ ] Spring Security with JWT authentication
- [ ] Department entity with `@ManyToOne` relationship
- [ ] Flyway for database migration management
- [ ] GitHub Actions CI/CD pipeline with Docker image registry
- [ ] Integration tests with `@SpringBootTest` and Testcontainers
- [ ] Caching with Spring Cache + Redis
- [ ] Actuator endpoints for health monitoring
- [ ] Kubernetes deployment manifests (YAML)
- [ ] Multi-environment configurations (dev, staging, prod)

---

