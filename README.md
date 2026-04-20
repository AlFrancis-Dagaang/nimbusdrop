# NimbusDrop

A secure file storage REST API built with Spring Boot. NimbusDrop allows users to upload, download, manage, and share files through a clean and well-structured API, backed by JWT-based authentication and role-based access control.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [Sample API Usage](#sample-api-usage)
- [How to Run](#how-to-run)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## Overview

NimbusDrop organizes files into logical containers called **Nimbuses**. Each Nimbus holds a collection of uploaded files called **Drops**. Users authenticate via JWT, and all operations are protected by role-based access control enforced at the API level.

---

## Features

- JWT-based authentication with access and refresh token support
- Role-based access control (RBAC) for resource protection
- File upload and download with metadata tracking
- Logical file grouping via Nimbus containers
- Secure per-user data isolation
- RESTful API design with consistent response structure

---

## Architecture

NimbusDrop follows a layered architecture pattern:

```
Client Request
     |
  Controller        (Handles HTTP, validates input, maps DTOs)
     |
  Service           (Business logic, authorization checks)
     |
  Repository        (JPA/Hibernate data access layer)
     |
  Database          (MySQL — entity persistence)
  File Storage      (Local or configurable file system)
```

Each layer has a single responsibility and communicates only with the layer directly below it.

---

## Tech Stack

| Layer            | Technology               |
|------------------|--------------------------|
| Language         | Java 17                  |
| Framework        | Spring Boot              |
| Security         | Spring Security + JWT    |
| ORM              | JPA / Hibernate          |
| Database         | MySQL                    |
| Build Tool       | Maven                    |

---

## API Endpoints

### Auth

| Method | Endpoint            | Description                          | Auth Required |
|--------|---------------------|--------------------------------------|---------------|
| POST   | `/auth/signup`      | Register a new user account          | No            |
| POST   | `/auth/login`       | Authenticate and receive JWT tokens  | No            |
| POST   | `/auth/refresh-jwt` | Obtain a new access token            | Refresh Token |

### Nimbus (File Containers)

| Method | Endpoint              | Description                    | Auth Required |
|--------|-----------------------|--------------------------------|---------------|
| POST   | `/nimbus`             | Create a new Nimbus container  | Yes           |
| GET    | `/nimbus/{id}`        | Retrieve a Nimbus by ID        | Yes           |
| PUT    | `/nimbus/{id}/name`   | Rename an existing Nimbus      | Yes           |
| DELETE | `/nimbus/{id}`        | Delete a Nimbus and its Drops  | Yes           |

### Drops (Files)

| Method | Endpoint                | Description                     | Auth Required |
|--------|-------------------------|---------------------------------|---------------|
| POST   | `/drops/upload`         | Upload a file to a Nimbus       | Yes           |
| GET    | `/drops/download/{id}`  | Download a file by ID           | Yes           |
| GET    | `/drops/{id}`           | Retrieve file metadata          | Yes           |
| DELETE | `/drops/{id}`           | Delete a file                   | Yes           |

### User

| Method | Endpoint                | Description                     | Auth Required |
|--------|-------------------------|---------------------------------|---------------|
| GET    | `/user/me`              | Get the authenticated user info | Yes           |
| POST   | `/user/change-password` | Update the user's password      | Yes           |

---

## Sample API Usage

### POST `/auth/login`

Authenticates a user and returns a JWT access token and refresh token.

**Request**

```http
POST /auth/login
Content-Type: application/json

{
    "email": "dabox81033@aperiol.com",
    "password": "admin123"
}
```

**Response** `200 OK`

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkYWJveDgxMDMzQGFwZXJpb2wuY29tIiwiaWF0IjoxNzc2NjcyNTA4LCJleHAiOjE3NzY2NzI1Njh9.2hZGTe10mte5TAUEz7vNhYleuBOm22joxYv_-SEDJDQ",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "userDisplayName": "MyNewUsername123",
      "email": "dabox81033@aperiol.com",
      "role": "USER"
    },
    "expiresAt": "2026-04-20 16:09:28"
  },
  "timestamp": "2026-04-20 16:08:28"
}
```

---

### POST `/drops/upload`

Uploads a file to a specified Nimbus container.

**Request**

```http
POST /drops/upload
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

file=@report.pdf
nimbusId=42
```

**Response** `201 Created`

```json
{
  "id": 101,
  "fileName": "report.pdf",
  "fileSize": 204800,
  "contentType": "application/pdf",
  "nimbusId": 42,
  "uploadedAt": "2025-04-20T10:35:00Z"
}
```

---

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

### Setup

**1. Clone the repository**

```bash
git clone https://github.com/your-username/nimbusdrop.git
cd nimbusdrop
```

**2. Configure the database**

Create only the database — Hibernate will auto-generate all tables on first run:

```sql
CREATE DATABASE nimbusdrop;
```

Copy the provided template and fill in your credentials:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then edit `application.properties` with your local values:

```properties
server.port=8085

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/nimbusdropdb
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# Hibernate auto-creates tables on first run
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=your_jwt_secret_key
app.jwt.expiration=60000
app.jwt.refresh-expiration=604800000

# SMTP (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password

# File Upload Limits
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

> `application.properties` is listed in `.gitignore` and will not be committed. Only `application.properties.example` is tracked by Git.

The following tables will be created automatically:

| Table                 | Description                              |
|-----------------------|------------------------------------------|
| `users`               | Registered user accounts                 |
| `nimbus`              | File container records                   |
| `nimbus_space`        | Storage quota and space tracking         |
| `drops`               | Uploaded file metadata                   |
| `drop_shared_links`   | Shareable file link records              |
| `verifacation_tokens` | Email verification and auth token store  |

**3. Build and run**

```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Future Improvements

- File sharing via expirable public links
- Storage quota enforcement per user
- Soft delete with a recovery/trash bin feature
- Cloud storage backend support (AWS S3, Google Cloud Storage)
- Pagination and filtering on Nimbus and Drop listings
- API rate limiting

---

## Author

**Al Francis Daga-ang**
[GitHub](https://github.com/AlFrancis-Dagaang) | [LinkedIn](https://www.linkedin.com/in/al-francis-daga-ang-734043348/)
