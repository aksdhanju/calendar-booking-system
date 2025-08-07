
# 📅 Calendar Booking System

A simple calendar booking system inspired by [cal.com](https://cal.com), allowing Calendar Owners to set their availability and Invitees to book appointments through a RESTful API.

---

## 🧾 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Assumptions](#assumptions)
- [Running Tests](#running-tests)
- [Design Decisions](#design-decisions)
- [Technology Stack](#technology-stack)

---

## 📌 Overview

This system enables Calendar Owners to define their availability, and Invitees to search and book 60-minute time slots without overlaps.

### Terminology

- **Calendar Owner**: A user who sets availability for bookings.
- **Invitee**: A user who books appointments with Calendar Owners.
- **Appointment**: A 60-minute booking made by an Invitee.

---

## 🚀 Features

- Set up availability rules for each day of the week
- Search available 60-minute time slots on a given day
- Book appointments from available slots
- Prevent double-booking
- View upcoming appointments

---

## 🛠 Getting Started

### Prerequisites

- Java 21
- Maven or Gradle

### Run the Application

```bash
./gradlew bootRun
```

### API Documentation

Once the server is running, access Swagger UI (if enabled) at:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 API Endpoints

### 1. Availability Setup (Calendar Owner)

```
POST /api/v1/availability/setup
```

### 2. Search Available Slots (Invitee)

```
GET /api/v1/availability/slots?ownerId={ownerId}&date=yyyy-MM-dd
```

### 3. Book Appointment (Invitee)

```
POST /api/v1/appointments/book
```

### 4. Upcoming Appointments (Calendar Owner)

```
GET /api/v1/appointments/upcoming?ownerId={ownerId}
```

---

## 💡 Assumptions

- Users (owners/invitees) are already authenticated.
- No authentication or session management is implemented.
- In-memory data storage is used; persistence is not required.
- Appointments are always 60 minutes, and must start at the top of the hour.

---

## 🧪 Running Tests

```bash
./gradlew test
```

Tests include unit coverage for:
- Availability slot generation
- Validation logic
- Appointment booking and conflict detection

---

## 🧱 Design Decisions

- Used Spring Boot for REST API and dependency injection.
- Applied strategy pattern for time validation.
- Used DTOs and validators for clean request validation.

---

## ⚙️ Technology Stack

- Java 21
- Spring Boot
- Lombok
- JUnit 5
- Gradle
