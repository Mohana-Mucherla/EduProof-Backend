# EduProof Backend
Spring Boot REST API for issuing and verifying academic certificates with QR codes and PDF generation.
## Tech Stack
- **Java 17** · **Spring Boot 3.4.5** · **Spring Security (JWT)** · **Spring Data JPA**
- **MySQL** (Aiven Cloud) · **ZXing 3.5.3** · **OpenPDF 1.3.43** · **Docker**
## Setup & Run
```bash
# Copy env and fill credentials
cp .env.example .env
# Run locally (port 9091)
./mvnw spring-boot:run
# OR with Docker
docker build -t eduproof-backend .
docker run -p 8080:8080 --env-file .env eduproof-backend
Environment Variables (.env)
DB_URL=jdbc:mysql://<host>:<port>/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
DB_USERNAME=avnadmin
DB_PASSWORD=your_password
Project Structure
src/main/java/com/eduproof/
├── config/       # Security, CORS, DataSeeder
├── controller/   # REST endpoints
├── dto/          # Request & Response objects
├── model/        # JPA entities
├── repository/   # Database queries
├── security/     # JWT filter & utils
└── service/      # Business logic
API Endpoints
Module	Base Path	Description
Auth	/api/auth	Register, Login, JWT
Dashboard	/api/dashboard	Stats per role
Students	/api/students	Student CRUD
Certificates	/api/certificates	Issue, download, manage
Verification	/api/verification	QR-based verification
Catalog	/api/catalog	Departments & cert types
Roles
Role	Access
ADMIN	Full access — students, certificates, dashboard
STUDENT	View & download own certificates
RECRUITER	Verify certificates by QR or ID
Notes
Certificate PDFs & QR images saved in uploads/
Tables auto-created via Hibernate ddl-auto=update
Default admin, departments & cert types seeded by DataSeeder on startup
