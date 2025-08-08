# 📡 API Endpoints
This document outlines the API endpoints for the Calendar Booking System, along with design decisions, assumptions, and request/response specifications.

---
## 1. (Availability Setup API) Create Availability Rules for an Owner

**Endpoint**  
```
POST /api/v1/availability/setup
```

**Request Body Fields**
- Contains the availability rules to be created.

| Field               | Type        | Validation                                                                                                                            | Description                               |
| ------------------- | ----------- |---------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| `ownerId`           | `String`    | Not blank, alphanumeric with `_` or `-`, max length 64                                                                                | User id for the calendar owner            |
| `rules`             | `List`      | Not null, max 30 rules, each validated individually                                                                                   | List of availability rules for this owner |
| `rules[].dayOfWeek` | `DayOfWeek` | Must be a valid Java `DayOfWeek` value (Possible values `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`) | Day of the week the availability applies  |
| `rules[].startTime` | `LocalTime` | Must be in `HH:mm` format. Example 16:00. Valid values from 00:00 to 23:00                                                            | Starting time for the availability slot   |
| `rules[].endTime`   | `LocalTime` | Must be in `HH:mm` format. Example 23:00                                                                                              | Ending time for the availability slot     |

**Sample Request Body** (JSON)
```json
{
  "ownerId": "1",
  "rules": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    }
}
```

**Possible Response Codes**
- `201 Created` → Rules successfully created.
```json
{
  "success": true,
  "message": "Availability rules created successfully for owner id: 1"
}
```
- `400 Bad Request` → Validation failed.
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```
- `409 Conflict` → Rules already exist.
```json
{
  "success": false,
  "message": "Availability rules already exist for owner: 1"
}
```
- `404 Not Found` → User not found.
```json
{
  "success": false,
  "message": "User not found with id: 1"
}
```

**Sample curl**
```
curl --location 'http://localhost:8080/availability/setup' \
--header 'Content-Type: application/json' \
--data '{
  "ownerId": "1",
  "rules": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "WEDNESDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "THURSDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "FRIDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "SATURDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "SUNDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    }
  ]
}'
```

Please refer to file createAvailabilityRulesScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/createAvailabilityRulesScenarios.txt
```

**Design Decisions/Assumptions/Information**

- I created 2 separate endpoints for create availability rules and update availability rules.
- Input validation is done at request DTO level using annotations provided by Jakarta Validation framework.
- Custom annotation class(@ValidAvailabilityRules) is created for specific validations. We cannot have any
field as null. We need start time and end time to be at full hour. We need start time to be before or equal to end time.
  No duplicate time slots are allowed.
- By default POST endpoint is not idempotent. If duplicate requests to create availability
rules come at same time, we use compare and swap approach(similar to optimistic locking)
and make sure we do not create 2 records in our data store(concurrent hash map in our case).
To implement above, computeIfAbsent method of ConcurrentHashMap is used.
- (How computeIfAbsent is working here) If the current value at hashmap's heap address is the expected value,
then it is replaced by the new value and return true. 
It means the last time we read this address and now, no other thread has modified this location.
If it is not the case, then it returns false. Means between last time we read
the expected value at this location and now, some other thread has modified this location.
So we do not modify this location and return false. This is real concurrency.
All this comparison is done in single, atomic assembly instruction.
During this time, we are sure that no other thread can interrupt our process.
This is essential for the CASing to work.
We can modify values at given location in memory without using synchronization tools.
- In case there is less thread contention, this approach is much more efficient than synchronization.
- Rules are stored per owner. Logic is added to method to merge overlapping slots/intervals. Ultimately when we save rules in data store,
we need them to be non overlapping. Though there can be more than 1 rule for a day.

## 2. (Availability Setup API) Update Availability Rules for an owner

**Endpoint**
```
PUT /api/v1/availability/setup
```

**Request Body Fields**
- Contains the new availability rules to be set/updated.

| Field               | Type        | Validation                                                                                                                            | Description                               |
| ------------------- | ----------- |---------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| `ownerId`           | `String`    | Not blank, alphanumeric with `_` or `-`, max length 64                                                                                | User id for the calendar owner            |
| `rules`             | `List`      | Not null, max 30 rules, each validated individually                                                                                   | List of availability rules for this owner |
| `rules[].dayOfWeek` | `DayOfWeek` | Must be a valid Java `DayOfWeek` value (Possible values `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`) | Day of the week the availability applies  |
| `rules[].startTime` | `LocalTime` | Must be in `HH:mm` format. Example 16:00. Valid values from 00:00 to 23:00                                                            | Starting time for the availability slot   |
| `rules[].endTime`   | `LocalTime` | Must be in `HH:mm` format. Example 23:00                                                                                              | Ending time for the availability slot     |

**Sample Request Body** (JSON)
```json
{
  "ownerId": "1",
  "rules": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    }
}
```

**Possible Response Codes**
- `201 Created` → If rules are newly created.
```json
{
  "success": true,
  "message": "Availability rules created successfully for owner id: 1"
}
```
- `200 OK` → if rules are updated.
```json
{
  "success": true,
  "message": "Availability rules updated successfully for owner id: 1"
}
```
- `400 Bad Request` → Validation failed.
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```
- `404 Not Found` → User not found.
```json
{
  "success": false,
  "message": "User not found with id: 1"
}
```

Please refer to file updateAvailabilityRulesScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/updateAvailabilityRulesScenarios.txt
```

**Sample curl**
```
curl --location --request PUT 'http://localhost:8080/availability/setup' \
--header 'Content-Type: application/json' \
--data '{
  "ownerId": "1",
  "rules": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "WEDNESDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "THURSDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "FRIDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "SATURDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    },
    {
      "dayOfWeek": "SUNDAY",
      "startTime": "16:00",
      "endTime": "23:00"
    }
  ]
}'
```

**Design Decisions/Assumptions/Information**
- This endpoint is similar to  create availability rules endpoint. It is PUT endpoint and by definition it 
is expected to be idempotent which it is. 
- I am assuming we are fine with lost updates as a read pheonomenon in this method. If 2 threads come 
of same owner id and try to update availability rules, the second threads changes would be persisted in DB
ultimately.
- This endpoint supports both create and update availability rules with different response codes.
- All input validations and other logic is similar as described in create availability rules endpoint.
- I am assuming creating and updating availability rules is a 1 time thing. So there would be less 
possibility of high concurrency in this endpoint.

## 3. Search Available Time Slots API

**Endpoint**
```
GET /api/v1/availability/{ownerId}/slots?date=yyyy-MM-dd
```

**Request Fields**
- **Path Parameter**
    - `ownerId` → User id for the owner.
- **Query Parameter**
    - `date` → Date for which available slots are to be fetched, in `yyyy-MM-dd` format.

**Sample Curl**
```
curl --location 'http://localhost:8080/api/v1/availability/1/slots?date=2025-08-08'
```
**Possible Response Codes**
- `200 OK` → (Case 1)No available time slots found
```json
{
  "success": true,
  "message": "No Available slots found for owner id: 1",
  "slots": []
}
```
- `200 OK` → (Case 2)Available slots fetched successfully
```json
{
  "success": true,
  "message":  "Available slots fetched successfully for owner id: 1",
  "slots": [
    {
      "startDateTime": "2025-08-06 16:00:00",
      "endDateTime": "2025-08-06 17:00:00"
    },
    {
      "startDateTime": "2025-08-06 17:00:00",
      "endDateTime": "2025-08-06 18:00:00"
    }
  ]
}
```
- `400 Bad Request` → Validation failed.
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```
- `404 Not Found` → User not found.
```json
{
  "success": false,
  "message": "User not found with id: 1"
}
```

Please refer to file getAvailableSlotsScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/getAvailableSlotsScenarios.txt
```

**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.


## 4. Book Appointment (Invitee)

**Endpoint**
```
POST /api/v1/appointments/book
```

**Request Fields**
- **Headers**
    - `Idempotency-Key` → Required string to ensure duplicate booking requests are safely ignored.
        - Allowed characters: letters, digits, hyphens (`-`), underscores (`_`).
        - Max length: 64 characters.
- **Request Body fields**

| Field              | Type        | Validation                                         | Description                                              |
|--------------------| ----------- |----------------------------------------------------|----------------------------------------------------------|
| `ownerId`          | `String`    | Not blank, alphanumeric with `_` or `-`, max length 64 | User id for the calendar owner                           |
| `inviteeId`        | `String`    | Not blank, alphanumeric with `_` or `-`, max length 64 | List of availability rules for this owner                |
| `startDateTime`    | `LocalDateTime` | Date format should be "yyyy-MM-dd HH:mm:ss"        | Date and time(slot) for which you want to do the booking |

**Sample Curl**
```
curl --location 'http://localhost:8080/appointments/book' \
--header 'Idempotency-Key: sampleKey' \
--header 'Content-Type: application/json' \
--data '{
  "ownerId": "1",
  "inviteeId" : "3",
  "startDateTime" : "2025-08-08 17:00:00"
}'
```
**Possible Response Codes**
- `201 CREATED` → Appointment created successfully in system
```json
{
  "success": true,
  "message": "Appointment booked successfully for owner id: 1",
  "appointmentId": "6cf5cfce-950b-4fdb-b680-355393303fca",
  "errorCode": null
}
```
- `200 OK` → Try with same idempotency key and Appointment already exists
```json
{
  "success": true,
  "message": "Appointment already exists for owner id: 1",
  "appointmentId": "6cf5cfce-950b-4fdb-b680-355393303fca",
  "errorCode": null
}
```
- `400 Bad Request` → Try with different idempotency key and Appointment already exists
```json
{
  "success": false,
  "message": "Appointment slot already booked for owner: 1"
}
```
- `400 Bad Request` → Validation errors in request
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```
- `404 Not Found` → User not found.
```json
{
  "success": false,
  "message": "User not found with id: 1"
}
```

Please refer to file bookAppointmentScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/bookAppointmentScenarios.txt
```

**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.

## 5. Get Upcoming Appointments (Calendar Owner)

**Endpoint**
```
GET /api/v1/appointments/owner/{ownerId}/upcoming?page={page}&size={size}
```

**Request Fields**
- **Path Parameter**
    - `ownerId` → User id for the owner.
- **Query Parameters**
    - `page` → Page index (default: 0).
    - `size` → Page size (default: 10, max: 100).  

- **Behavior**
    - Retrieves a paginated list of upcoming appointments for the specified owner.  

**Sample Curl**
```
curl --location 'http://localhost:8080/appointments/owner/2/upcoming'
```

**Sample Curl 2**
```
curl --location 'http://localhost:8080/appointments/owner/2/upcoming?page=0&size=10'
```

**Possible Response Codes**
- `200 OK` → Successfully fetch upcoming appointments
```json
{
  "success": true,
  "message": "Fetched upcoming appointments successfully for owner id: 1",
  "appointments": [
    {
      "appointmentId": "f954f74b-c4fb-4082-8b69-97939f9a5c5a",
      "startTime": "2025-08-06 16:00:00",
      "endTime": "2025-08-06 17:00:00",
      "inviteeId": "3",
      "inviteeName": "Akash",
      "inviteeEmail": "asingh@gmail.com"
    },
    {
      "appointmentId": "daf26453-d914-4604-937c-0f93dbfd4496",
      "startTime": "2025-08-06 18:00:00",
      "endTime": "2025-08-06 19:00:00",
      "inviteeId": "4",
      "inviteeName": "Prince",
      "inviteeEmail": "pkumar@gmail.com"
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalItems": 2
}
```
- `400 Bad Request` → Validation errors in request
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```
- `404 Not Found` → User not found.
```json
{
  "success": false,
  "message": "User not found with id: 1"
}
```

Please refer to file getUpcomingAppointmentsScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/getUpcomingAppointmentsScenarios.txt
```

**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.


## 6. Create User

**Endpoint**
```
POST /api/v1/users
```

**Request Body Fields**

| Field               | Type     | Validation                                                 | Description                    |
|---------------------|----------|------------------------------------------------------------|--------------------------------|
| `id`                | `String` | Not blank, alphanumeric with `_` or `-`, max length 64     | User id for the calendar owner |
| `name`              | `String` | Not blank, Name must be between 1 and 50 characters        | Name of calendar owner         |
| `email`             | `String` | Not blank, Email must be between 1 and 50 characters       | Email of calendar owner        |

**Sample Curl**
```
curl --location 'http://localhost:8080/api/v1/users' \
--header 'Content-Type: application/json' \
--data-raw '{
"id": "3",
"name": "Akash",
"email": "asingh@gmail.com"
}'
```

**Possible Response Codes**
- `201 Created` → User successfully created.
```json
{
  "success": true,
  "message": "User created successfully for id: 1"
}
```
- `409 Conflict` → User already exists
```json
{
  "success": false,
  "message": "User already exists with email: asingh@gmail.com"
}
```
- `400 Bad Request` → Validation failed.
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```

Please refer to file userScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/userScenarios.txt
```

**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.

## 6. Update User

**Endpoint**
```
PUT /api/v1/users
```
**Request Fields**
- **Path Parameter**
    - `ownerId` → User id for the owner.

**Request Body Fields**

| Field               | Type     | Validation                                                 | Description                    |
|---------------------|----------|------------------------------------------------------------|--------------------------------|
| `name`              | `String` | Not blank, Name must be between 1 and 50 characters        | Name of calendar owner         |
| `email`             | `String` | Not blank, Email must be between 1 and 50 characters       | Email of calendar owner        |

**Sample Curl**
```
curl --location --request PUT 'http://localhost:8080/users/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Galen",
    "email": "gsimmons@dealmeridian.com"
}'
```

**Possible Response Codes**
- `201 Created` → User successfully created.
```json
{
  "success": true,
  "message": "User created successfully for id: 1"
}
```
- `200 OK` → User already exists
```json
{
  "success": true,
  "message": "User updated successfully for id: 1"
}
```
- `400 Bad Request` → Validation failed.
```json
{
  "success": false,
  "message": "<some validation exception message>"
}
```

Please refer to file userScenarios.txt in below path for all test scenarios
```
src/test/java/com/company/calendar/info/userScenarios.txt
```

**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.




My Thoughts
On Optimistic vs Pessimistic locking
or Synchronized keyword vs Compare and Swap approach

