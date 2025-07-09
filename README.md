TRINITY (Backend)
This is the backend repository for the Trinity Password Manager, a secure application for storing and managing user passwords. It provides RESTful APIs for user authentication, password management, and administrative tasks. The backend is under active development, with the latest changes functional locally but not fully reflected in this repository.
Features

User Authentication: Supports registration, login, and JWT-based authentication with refresh tokens.
Password Management: Securely store, categorize, update, and delete passwords using AES encryption and BCrypt hashing.
Admin Dashboard: Provides insights into user activity, password statistics, and category distribution.
Secure Password Reset: Verification code-based password reset via email.
Category Management: Admin features for creating, updating, and deleting password categories.
Audit Logging: Tracks significant actions (e.g., user creation, password updates).
CORS Support: Configured for secure frontend communication (http://localhost:5173).

Technologies

Java 17: Core programming language.
Spring Boot 3.2.4: Framework for RESTful APIs.
Spring Security: JWT and role-based access control (USER, ADMIN).
PostgreSQL: Database for users, passwords, and categories.
JPA/Hibernate: Database operations and entity management.
BCrypt: Password hashing.
AES: Password encryption.
JavaMailSender: Email functionality for password resets.
Lombok: Reduces boilerplate code.
Maven: Dependency management and build tool.
SLF4J: Logging framework.

Project Structure
TRINITY/
├── src/main/java/com/project/Trinity/
│   ├── Config/                # Security and exception handling configurations
│   ├── Controller/            # REST controllers for auth, user, password, and admin
│   ├── DTO/                   # Data Transfer Objects for API requests/responses
│   ├── Entity/                # JPA entities (User, Password, Category, etc.)
│   ├── Filter/                # JWT authentication and authorization filters
│   ├── Repository/            # JPA repositories for database operations
│   ├── Service/               # Business logic for user, password, and email services
│   ├── Util/                  # JWT and AES encryption utilities
├── src/main/resources/
│   ├── application.properties # Database, JWT, and email configurations
├── pom.xml                    # Maven dependencies and build configuration

Setup Instructions

Prerequisites:

Java 17
Maven 3.9.9
PostgreSQL
SMTP server credentials (e.g., Gmail)


Clone the Repository:
git clone https://github.com/BeyzaNurKeskinn/TRINITY.git
cd TRINITY


Configure Environment:

Create a PostgreSQL database named trinity.
Update src/main/resources/application.properties:spring.datasource.url=jdbc:postgresql://localhost:5432/trinity
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your_base64_jwt_secret
encryption.secret-key=your_base64_aes_key
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password




Generate Secret Keys:

JWT secret:$RandomBytes = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Bytes = [byte[]]::new(32)
$RandomBytes.GetBytes($Bytes)
[Convert]::ToBase64String($Bytes)


AES encryption key:$RandomBytes = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Bytes = [byte[]]::new(16)
$RandomBytes.GetBytes($Bytes)
[Convert]::ToBase64String($Bytes)




Build and Run:
mvn clean install
mvn spring-boot:run

The backend runs on http://localhost:8080.

Test Endpoints:

POST /api/auth/register: Register a new user.
POST /api/auth/login: Log in and get tokens.
GET /api/user/passwords: Retrieve user passwords (authenticated).
GET /api/admin/dashboard: Admin dashboard data (ADMIN role required).



Development Status

Functional locally with features like JWT authentication, AES encryption, and category management.
Latest changes (e.g., enhanced password reset, audit logging) are not yet pushed to GitHub.
This repository serves as a template for future projects.

Future Improvements

Push local changes to GitHub.
Add unit and integration tests.
Implement rate limiting and two-factor authentication.
Optimize database queries.
Enhance error handling for production.

Contributing
Contributions are welcome! Fork the repository, create a branch, and submit a pull request.
License
Licensed under the Apache License, Version 2.0.
