
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

- Set up availability rules for each day of the week(configurable)
- Search available 60-minute time slots on a given day
- Book appointments from available slots
- Prevent double-booking
- View upcoming appointments for an owner

---

## 🛠 Getting Started

### Prerequisites

- Java 21
- Gradle
- 

### Run the Application

```bash
./gradlew bootRun
```

### API Documentation

Once the server is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 API Endpoints

### 1. (Availability Setup API) Create Availability Rules for an owner 

```
POST /api/v1/availability/setup
- **Request Body** (JSON)  
  - Contains the availability rules to be created.  
  - Must include `ownerId` and at least one rule.  
- **Behavior**  
  - Creates availability rules for the specified owner.  
  - Fails if rules already exist for that owner.  
- **Response**  
  - `201 Created` with success message if rules are created. 
```

### 2. (Availability Setup API) Update Availability Rules for an owner

```
PUT /api/v1/availability/setup
- **Request Body** (JSON)  
  - Contains the new availability rules to be set/updated.  
  - Must include `ownerId` and at least one rule.  
- **Behavior**  
  - Creates or overwrites availability rules for the specified owner.  
  - Idempotent — repeated calls with the same data will not create duplicates.  
- **Response**  
  - `200 OK` if rules are updated.  
  - `201 Created` if rules are newly created.
```

### 3. Search Available Time Slots API

```
GET /api/v1/availability/{ownerId}/slots?date=yyyy-MM-dd
- **Path Parameter**  
  - `ownerId` → Unique identifier for the owner.  
- **Query Parameter**  
  - `date` → Date for which available slots are to be fetched, in `yyyy-MM-dd` format.  

- **Behavior**  
  - Fetches all available time slots for the specified owner on the given date.  
  - Returns an empty list if no available slots are found.  

- **Response**  
  - `200 OK` with JSON containing:  
    - `success` → Boolean indicating request success.  
    - `message` → Informative message (e.g., "Available slots fetched successfully" or "No Available slots found").  
    - `slots` → Array of available slot objects (may be empty if none found).

```

### 4. Book Appointment (Invitee)

```
POST /api/v1/appointments/book
- **Headers**  
  - `Idempotency-Key` → Required string to ensure duplicate booking requests are safely ignored.  
    - Allowed characters: letters, digits, hyphens (`-`), underscores (`_`).  
    - Max length: 64 characters.  

- **Request Body** (JSON)  
  - Contains appointment details such as:
    - `ownerId` → Calendar owner’s ID.
    - `inviteeId` → ID of the person booking the appointment.
    - `startDateTime` → Appointment start time (ISO 8601 format).
    - Other appointment-specific fields.  

- **Behavior**  
  - Books an appointment with the specified owner for the given date/time.  
  - If the `Idempotency-Key` was already used with the same request, returns the previously created appointment without duplication.  

- **Response**  
  - `201 Created` → Appointment successfully created.  
  - `200 OK` → Appointment already exists for the given `Idempotency-Key` and details.  
  - JSON contains:
    - `success` → Boolean indicating operation result.
    - `message` → Status message.
    - `appointmentId` → Unique identifier for the appointment

```

### 4. Upcoming Appointments (Calendar Owner)

```
GET /api/v1/appointments/owner/{ownerId}/upcoming?page={page}&size={size}
- **Path Parameter**  
  - `ownerId` → Unique identifier for the calendar owner.  

- **Query Parameters**  
  - `page` → Page index (default: 0).  
  - `size` → Page size (default: 10, max: 100).  

- **Behavior**  
  - Retrieves a paginated list of upcoming appointments for the specified owner.  

- **Response**  
  - `200 OK` with JSON containing:
    - `success` → Boolean indicating operation result.  
    - `message` → Informative message.  
    - `appointments` → Array of upcoming appointment objects.  
    - Pagination metadata.  
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
