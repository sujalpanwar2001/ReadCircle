# ReadCircle - Book Sharing Platform

ReadCircle is a full-stack book sharing platform designed for managing personal collections and facilitating secure borrowing among users. This project demonstrates end-to-end development with production-ready deployment to Oracle Cloud Infrastructure (OCI), featuring real-time notifications, advanced security, and optimized performance.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [My Enhancements](#my-enhancements)
- [Getting Started](#getting-started)
- [Live Demo](#live-demo)
- [License](#license)
- [Contributors](#contributors)
- [Acknowledgments](#acknowledgments)

## Overview
ReadCircle enables users to register, validate accounts via email, manage their book collections, and borrow/return books with real-time notifications. Built with enterprise-grade security (Keycloak + JWT) and deployed to production on Oracle Cloud, this platform showcases scalable full-stack development with modern DevOps practices.

## Features
- **User Registration & Authentication**: Secure signup with email validation and role-based access control
- **Book Management**: Create, update, share, and archive personal book collections
- **Smart Borrowing**: Automated availability checks and borrow request workflows
- **Real-Time Notifications**: Instant WebSocket updates for borrow/return events
- **Return Management**: Streamlined return process with approval workflows
- **Performance Optimized**: Lazy loading, pagination, and caching for responsive experience


## Technologies Used

### Backend
- **Spring Boot 3** - Core web framework for RESTful APIs
- **Spring Security 6** - Comprehensive security implementation
- **JWT Token Authentication** - Stateless API security
- **Spring Data JPA** - ORM for database operations
- **Hibernate** - Object-relational mapping
- **JSR-303 Validation** - Bean validation framework
- **OpenAPI/Swagger** - API documentation and testing
- **WebSockets** - Real-time bidirectional communication

### Frontend
- **Angular 16** - Modern component-based framework
- **TypeScript** - Type-safe JavaScript development
- **Bootstrap 5** - Responsive UI framework
- **Lazy Loading** - Code splitting for performance
- **RxJS** - Reactive programming library
- **Angular Router Guards** - Route protection

### DevOps & Infrastructure
- **Docker & Docker Compose** - Containerization and orchestration
- **Oracle Cloud Infrastructure (OCI)** - Production deployment platform
- **GitHub Actions** - CI/CD pipelines
- **SonarCloud** - Code quality analysis (75% coverage achieved)
- **JUnit/Mockito** - Backend unit testing
- **DockerHub** - Container registry

## My Enhancements
This project extends an open-source learning template with production-grade features:

### 🔧 **Production Deployment**
- **OCI Integration**: Complete deployment to Oracle Cloud Infrastructure with auto-scaling and load balancing
- **Docker Orchestration**: Multi-container setup using Docker Compose for frontend, backend, and database
- **Live Hosting**: Production-ready deployment accessible at [http://readcircle.mine.bz/](http://readcircle.mine.bz/)

### ⚡ **Performance Optimizations**
- **Frontend Performance**: Implemented lazy loading and pagination, improving Lighthouse scores to 95+
- **API Response Optimization**: Cached frequent queries, reducing response times by 30%
- **Bundle Optimization**: Tree-shaking and code splitting for faster initial loads

### 🛡️ **Security & Quality**
- **CI/CD Pipeline**: Automated testing and deployment with GitHub Actions
- **Code Quality**: SonarCloud integration with 75%+ test coverage
- **Security Testing**: Validated Keycloak integration across 5 test scenarios

### 🧪 **Testing & Validation**
- **End-to-End Testing**: Validated complete user flows with 5 self-managed test accounts
- **Load Testing**: Stress-tested borrow/return workflows using JMeter
- **Security Validation**: Tested JWT token flows and role-based access controls

## Getting Started

### Prerequisites
- Java 17+ (for backend)
- Node.js 16+ (for frontend)
- Docker & Docker Compose
- Oracle Cloud account (optional, for production deployment)

### Backend Setup
1. Clone the repository
2. Navigate to `/backend` directory
3. Copy `application-dev.properties` to `application.properties`
4. Update database connection settings
5. Run `mvn spring-boot:run`
6. Access Swagger docs at `http://localhost:8080/swagger-ui.html`

### Frontend Setup
1. Navigate to `/frontend` directory
2. Install dependencies: `npm install`
3. Update API base URL in environment config
4. Start development server: `ng serve`
5. Access app at `http://localhost:4200`

### Docker Deployment
```bash
# Clone and navigate to project root
git clone https://github.com/sujalpanwar2001/ReadCircle.git
cd ReadCircle

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
