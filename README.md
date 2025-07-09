TRINITY (Backend)
This is the backend repository for the Trinity Password Manager, a secure application designed to store and manage user passwords with robust authentication and encryption mechanisms. The backend provides RESTful APIs to handle user authentication, password management, and administrative tasks. It is currently under development, with the latest changes working locally but not yet fully reflected in this repository.
Features
User Authentication: Supports registration, login, and token-based authentication using JWT (JSON Web Tokens) and refresh tokens.
Password Management: Allows users to securely store, categorize, update, and delete passwords with AES encryption for sensitive data and BCrypt for password hashing.
Admin Dashboard: Provides administrators with insights into user activity, password statistics, and category distribution.
Secure Password Reset: Implements a verification code-based password reset mechanism via email.
Category Management: Enables admins to create, update, and delete password categories.
Audit Logging: Tracks significant actions (e.g., user creation, password updates) for transparency.
CORS Support: Configured for secure communication with the frontend running on http://localhost:5173.
Technologies
Java 17: Core programming language.
Spring Boot 3.2.4: Framework for building RESTful APIs and managing dependencies.
Spring Security: Handles authentication and authorization with JWT and role-based access control (USER, ADMIN).
PostgreSQL: Database for storing users, passwords, categories, and tokens.
JPA/Hibernate: For database operations and entity management.
BCrypt: For secure password hashing.
AES: For encrypting sensitive password data.
JavaMailSender: For sending verification and password reset emails.
Lombok: Reduces boilerplate code.
Maven: Dependency management and build tool.
SLF4J: Logging framework for debugging and monitoring.
Project Structure
TRINITY/
├── src/main/java/com/project/Trinity/
│   ├── Config/                # Security, password encoding, and global exception handling configurations
│   ├── Controller/            # REST controllers for auth, user, password, category, and admin endpoints
│   ├── DTO/                   # Data Transfer Objects for API requests and responses
│   ├── Entity/                # JPA entities for User, Password, Category, RefreshToken, etc.
│   ├── Filter/                # Custom JWT authentication and authorization filters
│   ├── Repository/            # JPA repositories for database operations
│   ├── Service/               # Business logic for user, password, category, and email services
│   ├── Util/                  # Utility classes for JWT and AES encryption
├── src/main/resources/
│   ├── application.properties # Configuration for database, JWT, and email settings
├── pom.xml                    # Maven dependencies and build configuration
Setup Instructions
Prerequisites:
Java 17
Maven 3.9.9
PostgreSQL (local or managed instance)
SMTP server credentials (e.g., Gmail for email functionality)
Clone the Repository:
git clone https://github.com/BeyzaNurKeskinn/TRINITY.git
cd TRINITY

Configure Environment:
Create a PostgreSQL database named trinity.
Update src/main/resources/application.propertieswith your database credentials, JWT secret, encryption key, and SMTP settings:
spring.datasource.url=jdbc:postgresql://localhost:5432/trinity
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your_base64_encoded_jwt_secret
encryption.secret-key=your_base64_encoded_aes_key
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password
Generate Secret Keys (if needed):
For JWT secret:
$RandomBytes = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Bytes = [byte[]]::new(32)
$RandomBytes.GetBytes($Bytes)
[Convert]::ToBase64String($Bytes)
For AES encryption key:
$RandomBytes = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Bytes = [byte[]]::new(16)
$RandomBytes.GetBytes($Bytes)
[Convert]::ToBase64String($Bytes)
Build and Run:
mvn clean install
mvn spring-boot:run
The application will start on http://localhost:8080 (or the configured port).
Test Endpoints:
Use tools like Postman or cURL to test APIs such as:
POST /api/auth/register: Register a new user.
POST /api/auth/login: Log in and receive access/refresh tokens.
GET /api/user/passwords: Retrieve user passwords (requires authentication).
GET /api/admin/dashboard: Access admin dashboard data (requires ADMIN role).
Development Status
The backend is functional locally with the latest changes, including secure password storage, JWT authentication, and category management.
Recent updates (e.g., enhanced password reset, audit logging) are not yet pushed to this repository but are working in the local environment.
This repository serves as a foundation for the Trinity Password Manager and may be reused as a template for future projects.
Future Improvements
Push local changes to GitHub for version control.
Add unit and integration tests for robust validation.
Implement additional security features (e.g., rate limiting, two-factor authentication).
Optimize database queries for better performance.
Enhance error handling and logging for production readiness.
Contributing
This project is under active development. Contributions are welcome! Please fork the repository, create a new branch, and submit a pull request with your changes.
License
Licensed under the Apache License, Version 2.0. See the LICENSE file for details.
