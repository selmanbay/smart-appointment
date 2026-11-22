# Design flows for the SDD

Diagram sources for the SDD (IEEE 1016, 4+1 views) in Mermaid, written from the code and the contract. Paste a block into <https://mermaid.live> (or any Mermaid renderer) to export it as an image. Each diagram follows the SDD template's limits and comes with its design rationale.

**Legend.** Classes and calls marked `«planned»` belong to handoff tasks B4–B12 and are not implemented yet; everything else exists in the code (`backend/src/main/java/com/smartappointment/`). When a planned piece is built, update its diagram here in the same PR.

| SDD section | Diagram | Status |
|---|---|---|
| 3.1.1 Class diagram | [Logical view](#1-logical-view-class-diagram) | domain implemented; booking services planned |
| 3.2.1 Activity diagrams | [Booking validation](#21-activity-1-booking-validation-uc-3-step-6), [Status lifecycle](#22-activity-2-appointment-status-lifecycle-uc-8), [Waiting time and recommendation](#23-activity-3-waiting-time-estimate-and-staff-recommendation-uc-18-uc-19) | rules fixed by the SRS; code planned |
| 3.2.2 Sequence diagrams | [UC-3 Book](#31-sequence-1-uc-3-book-appointment), [UC-4 Reschedule](#32-sequence-2-uc-4-reschedule-appointment), [UC-8 Record status](#33-sequence-3-uc-8-record-service-status), [UC-2 Sign in](#34-supporting-sequence-uc-2-sign-in-implemented) | UC-2 implemented; others planned |
| 3.2.3 Data flow | [DFD level 0](#4-process-view-data-flow-diagram-level-0) | |
| 3.3 Development + physical | [Component and deployment](#5-development-and-physical-view-component-and-deployment-diagram) | implemented as in `docker-compose.yml` |

## 1. Logical view: class diagram

```mermaid
classDiagram
  direction LR

  class Organization {
    Long organizationId
    String name
    String timezone
    Map~String,String~ terminology
    boolean isActive
  }
  class AppUser {
    Long userId
    Long organizationId
    String fullName
    String email
    String passwordHash
    UserRole role
    String fcmToken
  }
  class UserRole {
    <<enumeration>>
    SYSTEM_ADMIN
    ORGANIZATION_OWNER
    STAFF
    CUSTOMER
  }
  class Staff {
    Long staffId
    Long userId
    String title
    int capacity
    boolean isActive
    Set~Service~ services
  }
  class Service {
    Long serviceId
    Long organizationId
    String name
    int expectedDurationMin
    BigDecimal learnedDurationMin
    int bufferMin
    boolean isActive
  }
  class AppointmentRule {
    Long serviceId
    Integer maxAdvanceDays
    Integer minLeadHours
    Integer cancelDeadlineHours
    Integer maxActivePerUser
  }
  class WorkingHours {
    Long staffId
    short dayOfWeek
    LocalTime startTime
    LocalTime endTime
    LocalTime breakStart
    LocalTime breakEnd
  }
  class ScheduleChangeRequest {
    Long staffId
    short dayOfWeek
    LocalTime newStartTime
    LocalTime newEndTime
    ScheduleChangeStatus status
  }
  class Appointment {
    Long appointmentId
    Long customerId
    Long staffId
    Long serviceId
    Instant startTime
    Instant endTime
    AppointmentStatus status
    Instant checkedInAt
    Instant serviceStartAt
    Instant serviceEndAt
  }
  class AppointmentStatus {
    <<enumeration>>
    BOOKED
    CHECKED_IN
    IN_SERVICE
    COMPLETED
    CANCELLED
    NO_SHOW
  }
  class Notification {
    Long appointmentId
    NotificationChannel channel
    NotificationType type
    NotificationStatus status
    String providerResponse
  }
  class Feedback {
    Long appointmentId
    short rating
    String comment
  }

  class AuthService {
    +login(LoginRequest) AuthResponse
  }
  class AccountService {
    +currentAccount(userId) AccountResponse
  }
  class AccessTokenService {
    +issue(AccountResponse) AccessToken
  }
  class BookingService {
    <<planned>>
    +book(BookingRequest) Appointment
    +reschedule(appointmentId, RescheduleRequest) Appointment
    +cancel(appointmentId) Appointment
  }
  class AppointmentStatusService {
    <<planned>>
    +changeStatus(appointmentId, AppointmentStatus) Appointment
  }
  class SlotService {
    <<planned>>
    +freeSlots(staffId, serviceId, date) List~Slot~
  }
  class StaffRecommender {
    <<planned>>
    +rank(serviceId, date) List~StaffOption~
  }
  class WaitTimeEstimator {
    <<planned>>
    +projectedStarts(appointments, now) List~Duration~
    +nextLearnedDuration(previous, actual) BigDecimal
  }
  class NotificationSender {
    <<interface>>
    +send(Notification) SendResult
  }

  Organization "1" --> "*" AppUser : belongs_to
  Organization "1" --> "*" Service : offers
  AppUser "1" --> "0..1" Staff : acts_as
  Staff "*" --> "*" Service : provides
  Staff "1" --> "*" WorkingHours : works
  Staff "1" --> "*" ScheduleChangeRequest : requests
  Service "1" --> "0..1" AppointmentRule : governed_by
  AppUser "1" --> "*" Appointment : books
  Staff "1" --> "*" Appointment : serves
  Service "1" --> "*" Appointment : of_type
  Appointment "1" --> "*" Notification : triggers
  Appointment "1" --> "0..1" Feedback : rated_by
  AppUser --> UserRole
  Appointment --> AppointmentStatus

  AuthService --> AccountService
  AuthService --> AccessTokenService
  BookingService --> SlotService
  BookingService --> NotificationSender
  StaffRecommender --> WaitTimeEstimator
  SlotService --> WaitTimeEstimator
  AppointmentStatusService --> WaitTimeEstimator
```

**Interpretation and design rationale**

- **Entities** map one-to-one to the eleven tables of SRS Table 9 (`staff_service` is the many-to-many between `Staff` and `Service`). Relationship names are the ones in the SRS ER diagram.
- **Feature packages own their entities.** Associations across features are held as ids (`Appointment.staffId`, not a `Staff` reference), so `booking`, `catalog` and `auth` stay independent; features cooperate only through services (`AuthService → AccountService → StaffLookupService`), which ArchUnit enforces.
- **Calculations are separate from persistence.** `WaitTimeEstimator` implements Eq. 3 and Eq. 4 as plain Java with no database or Spring dependency, so the recommendation can be unit-tested with the SRS examples.
- **External messaging sits behind `NotificationSender`,** so e-mail (SMTP) and push (FCM) can be swapped or mocked; a failed send is recorded in `Notification.status` and never rolls back the appointment.

## 2. Process view: activity diagrams

### 2.1 Activity 1: booking validation (UC-3 step 6)

```mermaid
flowchart TD
  A([Customer confirms a slot]) --> B[Begin transaction]
  B --> C{Staff provides<br/>the service and is active?}
  C -- no --> X1[Reject: STAFF_DOES_NOT_PROVIDE_SERVICE]
  C -- yes --> D{Service has an<br/>appointment rule?}
  D -- no --> G
  D -- yes --> E{"start ≥ now + minLeadHours<br/>and start ≤ today + maxAdvanceDays?"}
  E -- no --> X2[Reject: LEAD_TIME_NOT_MET /<br/>TOO_FAR_IN_ADVANCE]
  E -- yes --> F{"Customer's active bookings<br/>for this service < maxActivePerUser?"}
  F -- no --> X3[Reject: ACTIVE_LIMIT_REACHED]
  F -- yes --> G{Slot inside working hours<br/>and outside the break?}
  G -- no --> X4[Reject: OUTSIDE_WORKING_HOURS]
  G -- yes --> H[Lock the staff row<br/>SELECT … FOR UPDATE]
  H --> I{"Overlapping BOOKED / CHECKED_IN /<br/>IN_SERVICE appointments < capacity?"}
  I -- no --> X5[Reject: SLOT_TAKEN]
  I -- yes --> J[Insert appointment with status BOOKED]
  J --> K[Commit]
  K --> L[After commit: send confirmation<br/>and record the notification]
  L --> M([201 Created])
  X1 & X2 & X3 & X4 & X5 --> R[Roll back] --> N([409 with the rule code;<br/>client returns to slot selection])
```

**Design rationale.** The order checks cheap, request-level rules first and takes the lock only for the capacity check, so the lock is held for the shortest time. Counting and inserting under a row lock in one transaction is what guarantees SRS P2 ("capacity never exceeded under concurrent booking"); the rule codes are the `BookingProblem.code` values in the contract, so the client can tell the customer which rule failed (SRS U1). The confirmation is sent after commit so a mail failure cannot undo a booking.

### 2.2 Activity 2: appointment status lifecycle (UC-8)

```mermaid
stateDiagram-v2
  [*] --> BOOKED : book (UC-3)
  BOOKED --> BOOKED : reschedule (UC-4)<br/>new slot taken first
  BOOKED --> CANCELLED : cancel (UC-5)<br/>before cancelDeadlineHours
  BOOKED --> NO_SHOW : staff marks no-show
  BOOKED --> CHECKED_IN : staff records arrival<br/>sets checkedInAt
  CHECKED_IN --> IN_SERVICE : staff starts service<br/>sets serviceStartAt
  IN_SERVICE --> COMPLETED : staff completes<br/>sets serviceEndAt, updates learned duration (Eq. 3)
  COMPLETED --> [*]
  CANCELLED --> [*]
  NO_SHOW --> [*]
```

**Design rationale.** The allowed transitions are exactly those in SRS 2.5; any other request is rejected with `INVALID_STATUS`. The server stamps each time itself so the recorded durations that feed Eq. 3 cannot be edited by clients. Final states are never deleted, which keeps the history needed for analytics (F10) and the no-show rate. The same transition table is used by the web prototype (`web/src/api/statusTransitions.ts`) to enable only valid buttons.

### 2.3 Activity 3: waiting-time estimate and staff recommendation (UC-18, UC-19)

```mermaid
flowchart TD
  A([Customer picks a service and a date]) --> B[Find active staff who provide the service]
  B --> C[For each staff member: load the day's<br/>booked appointments in start order]
  C --> D["Learned duration per service:<br/>d_l = learned_duration_min, or expected_duration_min if no history"]
  D --> E["Eq. 4 for each appointment k:<br/>S_k = max(s_k, F_k-1), F_k = S_k + d_l,k<br/>F_0 = now (today) or opening time"]
  E --> F[Estimated wait of the next free slot = S − s]
  F --> G{More staff?}
  G -- yes --> C
  G -- no --> H[Sort by estimated wait]
  H --> I[Break ties by mean rating, no rating last]
  I --> J[Mark first as recommended:<br/>'Shortest estimated wait']
  J --> K([Return ranked StaffOption list])
```

**Design rationale.** The estimate uses only the schedule and recorded service times (SRS 1.4.4), so it can be recomputed on every request and is never stored. Learned durations come from Eq. 3 (EWMA, α = 0.2), which reacts to recent runs without being thrown off by one long appointment. Ranking by wait and then rating follows the SRS; the customer can still choose another staff member, and the reason is shown on screen.

## 3. Process view: sequence diagrams

Only the critical use cases are shown (SDD template); inclusions UC-18, UC-19 and UC-20 are part of UC-3 and UC-4.

### 3.1 Sequence 1: UC-3 Book appointment

| Use Case | Key Actors | Main Interactions |
|---|---|---|
| UC-3 Book Appointment (with UC-18, UC-19, UC-20) | Customer, Web app, Backend, PostgreSQL, SMTP | The customer sees staff ranked by estimated wait and free slots, confirms one, and the backend books it under a capacity lock and e-mails a confirmation. |

```mermaid
sequenceDiagram
  autonumber
  actor C as Customer
  participant W as Web app
  participant API as Backend API
  participant R as StaffRecommender «planned»
  participant S as SlotService «planned»
  participant B as BookingService «planned»
  participant DB as PostgreSQL
  participant N as NotificationSender «planned»

  C->>W: Select service and date
  W->>API: GET /services/{id}/staff-recommendations?date
  API->>R: rank(serviceId, date)
  R->>DB: staff, working hours, appointments, ratings
  R-->>API: StaffOption[] (wait, workload, recommended)
  API-->>W: 200 ranked staff
  C->>W: Select staff
  W->>API: GET /staff/{id}/slots?serviceId&date
  API->>S: freeSlots(staffId, serviceId, date)
  S->>DB: working hours, rule, booked appointments
  S-->>API: Slot[] with estimated wait
  API-->>W: 200 free slots
  C->>W: Select slot and confirm
  W->>API: POST /appointments (cookie + X-XSRF-TOKEN)
  API->>B: book(request)
  B->>DB: BEGIN, check rules, SELECT staff FOR UPDATE, count overlaps
  alt slot still free
    B->>DB: INSERT appointment (BOOKED), COMMIT
    B-->>API: Appointment
    API-->>W: 201 Created
    B-)N: after commit: send confirmation
    N->>DB: INSERT notification (SENT or FAILED)
  else rule failed or slot taken
    B->>DB: ROLLBACK
    API-->>W: 409 BookingProblem (code)
    W-->>C: Explain the rule, back to slot selection
  end
```

**Design rationale.** Recommendation, slots and booking are separate requests so each screen step stays fast (SRS P1 < 500 ms for reads) and the customer can change staff without re-booking. The booking re-checks everything in the transaction because the slot list may be stale by the time the customer confirms; that is the alternative course of UC-3 step 6. Notification is asynchronous after commit, matching SRS P3 and the rule that sending never undoes a booking.

### 3.2 Sequence 2: UC-4 Reschedule appointment

| Use Case | Key Actors | Main Interactions |
|---|---|---|
| UC-4 Reschedule Appointment (with UC-19, UC-20) | Customer, Web app, Backend, PostgreSQL, SMTP | The customer picks a new slot for an upcoming appointment; the backend takes the new slot before releasing the old one, in one transaction, and sends a notice. |

```mermaid
sequenceDiagram
  autonumber
  actor C as Customer
  participant W as Web app
  participant API as Backend API
  participant B as BookingService «planned»
  participant DB as PostgreSQL
  participant N as NotificationSender «planned»

  C->>W: Choose Reschedule on an upcoming appointment
  W->>API: GET /staff/{id}/slots?serviceId&date
  API-->>W: 200 free slots with estimated wait
  C->>W: Select new staff and slot, confirm
  W->>API: POST /appointments/{id}/reschedule
  API->>B: reschedule(id, staffId, start)
  B->>DB: BEGIN, load appointment (owner, status BOOKED)
  alt before cancelDeadlineHours
    B->>DB: check rules, SELECT new staff FOR UPDATE, count overlaps
    alt new slot free
      B->>DB: UPDATE appointment SET staff, start, end, COMMIT
      B-->>API: Appointment
      API-->>W: 200 moved
      B-)N: after commit: send reschedule notice
    else new slot taken
      B->>DB: ROLLBACK (original appointment kept)
      API-->>W: 409 SLOT_TAKEN
    end
  else deadline passed
    API-->>W: 409 CANCEL_DEADLINE_PASSED
  end
```

**Design rationale.** Moving the existing row instead of cancelling and re-creating keeps one appointment history and makes the operation atomic: the old slot is released only if the new one is taken (UC-4 post-condition). The deadline is checked before any lock so late requests fail fast. The same capacity check as booking prevents a reschedule from overbooking the new staff member.

### 3.3 Sequence 3: UC-8 Record service status

| Use Case | Key Actors | Main Interactions |
|---|---|---|
| UC-8 Record Service Status | Staff, Web app, Backend, PostgreSQL | The staff member records check-in, start and completion (or no-show); the backend stamps the times and, on completion, updates the learned service duration. |

```mermaid
sequenceDiagram
  autonumber
  actor S as Staff
  participant W as Web app
  participant API as Backend API
  participant A as AppointmentStatusService «planned»
  participant E as WaitTimeEstimator «planned»
  participant DB as PostgreSQL

  S->>W: Open daily agenda
  W->>API: GET /staff/{id}/agenda?date
  API->>DB: appointments of the day in time order
  API-->>W: 200 agenda
  S->>W: Check in / Start service / Complete / No-show
  W->>API: POST /appointments/{id}/status {status} (cookie + X-XSRF-TOKEN)
  API->>A: changeStatus(id, status)
  A->>DB: load appointment (must belong to this staff member)
  alt transition allowed (SRS 2.5)
    A->>DB: UPDATE status and timestamp (checked_in_at / service_start_at / service_end_at)
    opt status = COMPLETED
      A->>E: nextLearnedDuration(previous, actual)
      E-->>A: new learned duration (Eq. 3)
      A->>DB: UPDATE service.learned_duration_min
    end
    A-->>API: Appointment
    API-->>W: 200 updated
  else not allowed
    API-->>W: 409 INVALID_STATUS
  end
```

**Design rationale.** Status changes go through one service with a transition table, so the web buttons, the API and the database check agree. The learned duration is updated in the same transaction as the completion, so the next waiting estimate (Activity 3) already uses it. Times are taken from the server clock, not the client, which keeps durations comparable across devices.

### 3.4 Supporting sequence: UC-2 Sign in (implemented)

| Use Case | Key Actors | Main Interactions |
|---|---|---|
| UC-2 Log In | User (any role), Web app, Backend, PostgreSQL | The user signs in with e-mail and password; the backend returns the account and sets an HttpOnly token cookie used by every later request. |

```mermaid
sequenceDiagram
  autonumber
  actor U as User
  participant W as Web app
  participant SF as Security filters
  participant AC as AuthController
  participant AS as AuthService
  participant T as AccessTokenService
  participant DB as PostgreSQL

  U->>W: E-mail and password
  W->>AC: POST /api/v1/auth/login
  AC->>AS: login(request)
  AS->>DB: find app_user by e-mail
  AS->>AS: PasswordEncoder.matches (also when the e-mail is unknown)
  alt valid
    AS->>T: issue(account)
    T-->>AS: signed JWT (sub, role, organizationId, staffId, exp)
    AS-->>AC: AuthResponse
    AC-->>W: 200 + Set-Cookie sa_access (HttpOnly, SameSite=Strict, Path=/api)
  else invalid
    AC-->>W: 401 "Invalid e-mail or password."
  end
  W->>SF: GET /api/v1/me (cookie sent by the browser)
  SF->>SF: CsrfFilter (GET is safe) → cookie to bearer → verify JWT
  SF-->>W: XSRF-TOKEN cookie for later POSTs
  SF->>AC: authenticated request
  AC-->>W: 200 Account
```

**Design rationale.** The session is the signed token, so the backend stays stateless (ADR-0008). The token sits in an HttpOnly cookie that scripts cannot read, and every state-changing request must also carry the CSRF header (ADR-0015). An unknown e-mail takes the same path as a wrong password so responses do not reveal which accounts exist.

## 4. Process view: data flow diagram (level 0)

```mermaid
flowchart LR
  CU[Customer]:::ext
  ST[Staff]:::ext
  OW[Organization Owner]:::ext
  AD[System Admin]:::ext
  SMTP[SMTP e-mail server]:::ext
  FCM[Firebase Cloud Messaging]:::ext

  P1((1. Authenticate))
  P2((2. Manage organizations,<br/>services, staff, hours, rules))
  P3((3. Book, reschedule,<br/>cancel))
  P4((4. Record service status))
  P5((5. Estimate wait and<br/>recommend staff))
  P6((6. Notify))
  P7((7. Report analytics))

  D1[(D1 Accounts)]
  D2[(D2 Organizations, services, rules)]
  D3[(D3 Staff and schedules)]
  D4[(D4 Appointments)]
  D5[(D5 Notifications)]
  D6[(D6 Feedback)]

  CU & ST & OW & AD -- credentials --> P1
  P1 -- token, account --> CU & ST & OW & AD
  P1 <--> D1
  AD -- organization data --> P2
  OW -- services, staff, hours, rules --> P2
  P2 --> D2 & D3
  CU -- service, date, slot choice --> P3
  P3 -- free slots, confirmation --> CU
  P3 <--> D4
  P3 --> D2 & D3
  P5 -- ranked staff, estimated wait --> P3
  P5 --> D3 & D4 & D6
  ST -- check-in, start, complete, no-show --> P4
  P4 <--> D4
  P4 -- learned duration --> D2
  P3 & P4 -- events --> P6
  P6 -- e-mail --> SMTP
  P6 -- push --> FCM
  SMTP & FCM -- accepted / rejected --> P6
  P6 --> D5
  CU -- rating, comment --> D6
  OW -- period --> P7
  P7 --> D4 & D6
  P7 -- rates, occupancy, wait --> OW

  classDef ext fill:#eef,stroke:#557
```

**Design rationale.** The diagram shows the four roles and the two external messaging services from the SRS context diagram (Figure 1) as the only external entities. Appointments (D4) are the central store: booking writes them, status recording completes them, and both the estimate and analytics read them, which is why recorded service times are required (SRS 1.4.5).

## 5. Development and physical view: component and deployment diagram

```mermaid
flowchart TB
  subgraph Client devices
    BR[Browser<br/>React web app]
    AN[Android phone<br/>Kotlin app «planned»]
  end

  subgraph Host["Host (docker compose locally; one VM or container platform when hosted)"]
    subgraph WEB[web container: nginx]
      WEBAPP[Static React build<br/>/ → index.html, /api → backend]
    end
    subgraph BE[backend container: Java 21, Spring Boot]
      direction TB
      SEC[common.security<br/>JWT, cookie, CSRF, CORS]
      RL[common.ratelimit]
      WEBERR[common.web<br/>RFC 7807, correlation id]
      AUTH[auth]
      ORG[organization]
      CAT[catalog]
      SCH[schedule]
      BOOK[booking]
      EST[estimation<br/>plain Java]
      NOTI[notification]
      FB[feedback]
      ANA[analytics]
    end
    DB[(postgres container<br/>PostgreSQL 17)]
    MAIL[mailpit container<br/>SMTP in development]
  end

  FCMX[Firebase Cloud Messaging]
  SMTPX[SMTP server when hosted]

  BR -- HTTPS, cookie + XSRF header --> WEBAPP
  AN -- HTTPS, bearer token --> SEC
  WEBAPP -- /api --> SEC
  SEC --> RL --> AUTH & BOOK & CAT & SCH & ORG & FB & ANA
  AUTH --> CAT
  BOOK --> CAT & SCH & EST & NOTI
  ANA --> BOOK & FB
  AUTH & ORG & CAT & SCH & BOOK & NOTI & FB -- JDBC, Flyway --> DB
  NOTI -- SMTP --> MAIL
  NOTI -. hosted .-> SMTPX
  NOTI -. push .-> FCMX
```

- **Components follow the feature packages** (ADR-0001): each owns its entities, service and controller; cross-cutting concerns live in `common`. Dependencies point one way (no cycles, checked by ArchUnit), and `estimation` depends on nothing but Java.
- **Deployment is the same images everywhere** (ADR-0008): `web` (nginx serving the React build and proxying `/api`), `backend` (stateless, so more instances can run behind a load balancer), `postgres`, and `mailpit` in development replaced by a real SMTP server when hosted; configuration comes from environment variables.
- **Clients:** the browser uses the session cookie with CSRF protection through the same origin; the Android app calls the API directly with a bearer token (ADR-0015).
