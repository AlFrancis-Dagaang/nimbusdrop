# NimbusDrop

> A Secure File Storage SpringBoot REST API - Upload, download, and manage files with enterprise-grade security.


## Overview

NimbusDrop is a robust REST API file storage system built with Spring Boot that provides secure file upload, download, and management capabilities. Designed with security-first principles, it offers JWT-based authentication, role-based access control, and scalable file storage architecture for modern cloud applications.

## Key Features

- **JWT Authentication** - Secure token-based authentication using Nimbus JOSE + JWT
- **File Management** - Upload, download, delete, and list files via RESTful endpoints
- **Role-Based Access Control** - Granular permissions for users and administrators
- **Multiple Storage Options** - Local filesystem or cloud storage (S3, Azure Blob)
- **File Metadata Tracking** - Store and retrieve file metadata (size, type, upload date)
- **Secure File Access** - Presigned URLs and access control for file downloads
- **High Performance** - Optimized for handling large files with streaming support
- **Input Validation** - Comprehensive file type and size validation

## 🛠️ Tech Stack

- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database persistence
- **Nimbus JOSE + JWT** - JSON Web Token implementation
- **PostgreSQL/MySQL** - Relational database
- **Maven** - Dependency management
- **Lombok** - Boilerplate code reduction
