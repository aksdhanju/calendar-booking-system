# 📡 API Endpoints
This document outlines the API endpoints for the Calendar Booking System, along with design decisions, assumptions, and request/response specifications.

Please refer to file successUserJourney.txt in below path for a general success user journey
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/successUserJourney.txt" target="_blank">src/test/java/com/company/calendar/info/successUserJourney.txt</a>

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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/createAvailabilityRulesScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/createAvailabilityRulesScenarios.txt</a>


**Design Decisions/Assumptions/Information**

- I created 2 separate endpoints for create availability rules and update availability rules.
- Input validation is done at request DTO level using annotations provided by Jakarta Validation framework.
- Custom annotation class(@ValidAvailabilityRules) is created for specific validations. We cannot have any
field as null. We need start time and end time to be at full hour. We need start time to be before or equal to end time.
  No duplicate time slots are allowed.
- the behaviour of endpoint is that on calling it for first time, rules will be created. On calling it for second time,
it would throw an error that rules already exist. (C in CRUD)
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
- Currently I am assuming there is no specific rule for a particular date. Lets say there is vacation on 15 August, 2025
Currently rules are dependent on day of week, start time, end time. If in future we want to have rules for specific days,
we would need to add day field in AvailabilityRuleRequest dto and AvailabilityRule entity
- Note: If dayOfWeek = MONDAY, startTime = 20:00, endTime = 23:00 in AvailabilitySetupRequest, it would mean we want to create rules 
for 4 slots 20:00 to 21:00, 21:00 to 22:00, 22:00 to 23:00, 23:00 to 00:00(next day). 
Please note 23:00 to 00:00 is also considered a slot for above request.

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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/updateAvailabilityRulesScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/updateAvailabilityRulesScenarios.txt</a>


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
- This endpoint is similar to create availability rules endpoint. It is PUT endpoint and by definition it 
is expected to be idempotent which it is. 
- On first time hitting this endpoint, it would create a rule. On second time hitting the endpoint, it would update the rule.
- I am not keeping this method as PATCH. It can be a future requirement. As per this method, you update entire resource.
PATCH allows partial updates to resource which is not allowed as of now.
- I am assuming we are fine with lost updates as a read pheonomenon in this method. If 2 threads come 
of same owner id and try to update availability rules, the last threads changes would be persisted in DB
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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/getAvailableSlotsScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/getAvailableSlotsScenarios.txt</a>


**Design Decisions/Assumptions/Information**
- This endpoint is a GET endpoint. 
- As per problem description, we are asked to implement an API endpoint that allows an Invitee to
search for available time slots on a particular date. Actually in cal.com, its a month but for now we are supporting date.
- Whenever we are making a GET endpoint, we should think of pagination because
  want to return a finite amount of data in response. But because this endpoint is expected to return available slots for 
a particular date(which means day like MONDAY), I am safely assuming at max for a day there would be 24 slots.
Reason being we are already merging intervals in create/update availability rules endpoint and doing input validation on those rules.
Hence pagination is not implemented in this endpoint.
- To get available slots for an owner for a particular date, we use following formula:
Available slots(3) = Total slots(1) - Booked slots(2)
  - Total slots(1): We already had rules created for owner. We fetch total slots for a particular day.
  - Booked slots(2): These are slots which are booked by invitee for her/his meeting with owner. They were booked using
  Book appointment API by invitee. These slots are not available now because they are booked. And they should be removed.
  - generateAvailableSlotsFromRules method has core logic to calculate (3) above. It has special check for endTime being midnight 
  and it's the last slot of day 
    - Only consider rules which has day same as that of date
    - booked slots would always be a subset of total slots(1). This is an important **assumption** I am making. Via frontend, call
    will first go to Search Available Time Slots API. From these available slots, invitee will select one of the slot and call
      Book Appointment (Invitee) api to book a slot. So it would be never possible that booked start times are not present in start date time of total slots.

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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/bookAppointmentScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/bookAppointmentScenarios.txt</a>


**Design Decisions/Assumptions/Information**
- I have created this endpoint as POST for now. As of now, no support is provided to update appointment.
- For POST endpoints, we need to explicitly handle idempotency. This is achieved by passing Idempotency-Key in request header.
We use appointmentIdempotencyStore and appointmentIdempotencyLockManager to synchronize access to core booking logic.
Reason is because of network errors, backend latency, retries, it may be possible that duplicate request goes to same endpoint
with same request body and headers
- Once we ensure request is not duplicate, we do validations on appointment
- Once request is validated, we move to do the booking. There are 2 approaches - using Pessimistic and Optimistic locking. This is 
configurable using config key - appointment.booking-strategy. It has 2 possible values - pessimistic and optimistic
- Once booking is done, we update idempotency key in idempotency store, release lock for the same and return success response.

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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/getUpcomingAppointmentsScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/getUpcomingAppointmentsScenarios.txt</a>

**Design Decisions/Assumptions/Information**
- This is a GET endpoint. We are required to return all the upcoming appointments for an owner after current date(including current date)
- I have implemented pagination support in this endpoint since get endpoint response can be quite large
- We are also returning user metadata along with other fields related to appointment in response

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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/userScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/userScenarios.txt</a>

**Design Decisions/Assumptions/Information**
- user entity will have id and other metadata. This is as per description in problem statement.
We are keeping UserMetadata in a separate class considering Single responsibility principle. If in future 
new metadata fields are added, we should not change User class.
- We do usual input validation on input request.
- This is a POST endpoint. If we hit endpoint for first time, new user is created. We get 201 CREATED response for the same.
On hitting endpoint again second time with same payload, we get 409 Conflict because user may already exist with this email.
- If 2 threads try to create user with same paylaod at same time, we ensure only one will succeed using locking mechanisms at repository layer.


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
<a href="https://github.com/aksdhanju/calendar-booking-system/blob/main/src/test/java/com/company/calendar/info/userScenarios.txt" target="_blank">src/test/java/com/company/calendar/info/userScenarios.txt</a>

**Design Decisions/Assumptions/Information**
- update user is a PUT endpoint. Since we are updating resource.
- We do usual input validation on input request.
- If we hit endpoint for first time, new user is created. We get 201 CREATED response for the same.
  On hitting endpoint again second time with same payload, we get 200 OK. We override already existing user for same id.
- But if our request has an email id for a user id and in our storage, already an email id is associated with another user id, we return 409 Conflict(User already exists)
- email id is like unique key of user entity/table. So we are maintaining a email to user mapping in our table. 
While saving id to user mapping in our storage, we also store email to user mapping in our storage. We ensure this operation is synchronized.
- If 2 threads try to create user with same payload at same time, we are ok with lost updates here. The last thread will win in this case.
Our only concern area is that writes to storage should be atomic and should not lead to inconsistency in storage layer. Hence we used ConcurrentHashMap
and also synchronized the block in which 2 writes are happening.


