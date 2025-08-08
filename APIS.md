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


**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.


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

**Design Decisions**
- Rules are stored per owner and do not overlap existing ones.
- Validation is handled at the request DTO level.

**Assumptions**
- An owner can have only one set of rules at a time.
- No duplicate time slots are allowed.