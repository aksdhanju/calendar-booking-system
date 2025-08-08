
# 📅 Calendar Booking System

A simple calendar booking system inspired by [cal.com](https://cal.com), allowing Calendar Owners to set their availability and Invitees to book appointments through a RESTful API.

---

## 🧾 Table of Contents

- [Overview](#Overview)
- [Features](#Features)
- [Prerequisites](#Prerequisites)
- [Setup Instructions](#Setup-Instructions)
- [API Endpoints](#api-endpoints)
- [Assumptions](#overall-assumptions)
- [Running Tests](#running-tests)
- [Design Decisions](#design-decisions)
- [Technology Stack](#technology-stack)

---

## Overview

This system enables Calendar Owners to define their availability, and Invitees to search and book 60-minute time slots without overlaps.

### Terminology

- **Calendar Owner**: A user who sets availability for bookings.
- **Invitee**: A user who books appointments with Calendar Owners.
- **Appointment**: A 60-minute booking made by an Invitee.

---

## Features

- Set up availability rules for each day of the week(configurable)
- Search available 60-minute time slots on a given day
- Book appointments from available slots
- Prevent double-booking
- View upcoming appointments for an owner

---

## Prerequisites
- Java 21 → Ensure JAVA_HOME is set to Java 21.
- Gradle → You can use the included Gradle wrapper (./gradlew) without installing Gradle globally.
- Git → For cloning the repository.
- IDE (Optional) → IntelliJ IDEA, Eclipse, or VS Code with Java support.
- No database setup is required.

## Setup Instructions

Follow the steps below to set up and run the application locally.

### 1. Clone the Repository
```bash
git clone https://github.com/aksdhanju/calendar-booking-system.git
cd calendar-booking-system
```

### 2. Storage
```bash
The application uses in-memory HashMaps for storing availability and appointment data.
No database setup is required.
Data will be lost when the application restarts.
```

### 3. Build the Application

```bash
./gradlew clean build --refresh-dependencies
```

### 4. Run the Application

```bash
./gradlew bootRun
```
The application will start at:
```bash
http://localhost:8080
```
Note: To change port to new port(example 8081)
Do below change in application.yml
```bash
server.port: 8081
```

To kill current process running on port 8080
```bash
lsof -i:8080
kill -9 {process_id}
```

### 5. Access API Documentation

Once the server is running, access Swagger UI at:

```
http://localhost:8080/api/v1/swagger-ui/index.html
```

---
## API Endpoints
Note: Please refer to [APIS.md](./APIS.md) file for endpoint specific details

---

## Assumptions
Note: Please refer to endpoint specific assumptions in [APIS.md](./APIS.md) file

- Users (owners/invitees) are already authenticated.
- No authentication or session management is implemented.
- In-memory data storage is used; persistence is not required.
- Appointments are always 60 minutes, and must start at the top of the hour. Though its configurable.

---

## Running Tests

Tests include unit coverage for:
- Availability slot generation
- Validation logic
- Appointment booking and conflict detection

Run following gradle command to run tests:
```bash
./gradlew test
```

Please refer to files(.txt) in below path for various test scenarios
<a href="https://github.com/aksdhanju/calendar-booking-system/tree/main/src/test/java/com/company/calendar/info" target="_blank">src/test/java/com/company/calendar/info/</a>

---

## Design Decisions
Note: Please refer to endpoint specific design decisions in [APIS.md](./APIS.md) file

- Tried to incorporate SOLID principles, design patterns (Strategy Pattern) while creating classes and DTOs.
- Designed code to be **extensible** for future requirement changes.
- Followed **modular and maintainable** approach using layered architecture of a Spring Boot application (Controller → Service → Repository).
- Used **Spring Boot** for REST API, dependency injection, and configuration management.
- Applied **Strategy Pattern** for time validation logic to support multiple booking strategies in the future.
- Used **DTOs and custom validators** for strict and clean request validation.
- Implemented **centralized exception handling** using `@ControllerAdvice` for consistent error responses.
- Adopted **OpenAPI/Swagger** for clear API documentation.
- Ensured **separation of concerns** between persistence logic, business logic, and presentation.
- Optimized for **concurrency safety** in appointment booking using optimistic locking.
- Incorporated **unit tests** and **integration tests** to validate functionality and prevent regressions.
- Structured project for **easy onboarding** — meaningful package structure, descriptive class names, and in-code comments.
- Chose **Java Time API** (`LocalDate`, `LocalTime`, `DayOfWeek`) to avoid timezone pitfalls.
- Designed APIs to return **consistent response structure** (status, message, data) for both success and error scenarios.
- Ensured **scalability readiness** — services are stateless and can be deployed in multiple instances.
- Self explanatory debug,info,warn,error logs added at various places in the application

---

## Technology Stack

- **Programming Language**:
    - Java 21
- **Backend Framework**:
    - Spring Boot 3.5.4 – Core application framework
    - Spring Web – For building REST APIs
    - Spring Validation (Jakarta Validation) – For request validation
    - Springdoc OpenAPI 2.6.0 – API documentation (Swagger UI)
- **Utilities & Libraries**:
    - Lombok – Reduces boilerplate code
- **Build & Dependency Management**:
    - Gradle – Build automation tool
- **Testing**:
    - JUnit 5 – Unit and integration testing
    - Mockito – Mocking framework
- **Logging & Monitoring**:
  - SLF4J with Logback – Logging framework
- **Version Control**:
  - Git – Version control system 
  - GitHub – Code hosting