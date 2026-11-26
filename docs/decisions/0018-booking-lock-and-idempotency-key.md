# ADR-0018: Bookings lock the staff row; the Idempotency-Key is stored in its own table

- **Status:** Proposed
- **Date:** 2026-11-26
- **Feature:** [docs/features/booking.md](../features/booking.md)

## Context

SRS P2 and AGENT_RULES section 8 require that a staff member's capacity is never exceeded, even when
several customers book the last free place at the same moment. The handoff (B8, section 5) asked for
an ADR on the locking strategy and proposed `SELECT ... FOR UPDATE` on the staff row. The contract's
`bookAppointment` also takes an optional `Idempotency-Key` header: repeating a request with the same
key must return the first appointment instead of booking twice. `V1__initial_schema.sql` has no place
to remember a key.

## Decision

- **Lock:** `BookingService.book` runs in one transaction. After the request-level rules (staff
  provides the service, lead time, advance limit, active-booking limit, working hours; SDD Activity
  1), it locks the staff row with `SELECT ... FOR UPDATE` (`StaffRepository.lockById`, a
  `PESSIMISTIC_WRITE` query called through `StaffLookupService.lockStaffMember`, which requires a
  running transaction). Only then does it count the overlapping BOOKED / CHECKED_IN / IN_SERVICE
  appointments and insert. PostgreSQL's default READ COMMITTED isolation gives each statement after
  the lock a fresh snapshot, so the count sees every booking committed before the lock was granted.
- **Idempotency-Key:** a new table `booking_idempotency_key` (`V2__booking_idempotency_key.sql`)
  stores `(customer_id, idempotency_key) -> appt_id`, unique per customer. The key is looked up
  before any rule, and again right after the lock and before the capacity count, so a duplicate that
  waited for the lock returns the first appointment instead of failing with `SLOT_TAKEN`.

## Consequences

- `BookingConcurrencyIT` fires 8 parallel requests for the last free place: exactly one gets 201,
  the others get 409 `SLOT_TAKEN`. With the lock removed, the same test fails (more than one 201),
  so it does catch the race.
- Bookings of different staff members never wait for each other; bookings of the same staff member
  run one after another for the few milliseconds of count and insert.
- The active-booking limit is checked before the lock (SDD order). Two parallel requests by the same
  customer for **different** staff members can still both pass it; the lock does not cover that case.
- A replayed request returns the appointment as it is now (it may have been cancelled since), with
  `estimatedWaitMinutes` left out.
- Keys are never deleted yet. A clean-up job (for example keys older than a day) can come later.
- Cancel and reschedule (B9) lock the appointment row first, so the two cannot interleave on one
  appointment. Reschedule then locks the new staff member's row and counts capacity without the
  appointment being moved.

## Alternatives considered

- PostgreSQL advisory lock on the staff id: works as well, but is invisible in the schema and in
  JPA, and easy to forget when the reschedule (B9) takes the same lock.
- SERIALIZABLE isolation with retries: no explicit lock, but every caller needs retry logic and a
  conflict surfaces as a generic serialization error instead of `SLOT_TAKEN`.
- An exclusion constraint on overlapping time ranges: cannot express capacity > 1.
- Keeping idempotency keys in memory: breaks the stateless, hosting-ready rule (ADR-0008).
