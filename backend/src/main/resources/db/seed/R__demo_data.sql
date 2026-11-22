-- Demo data for local runs and the MVP demo (handoff B2). Loaded only by the `local` profile
-- (spring.flyway.locations in application-local.yml); never in tests or prod.
--
-- Accounts (all fictional, e-mail domain example.com). Every demo account uses the same fake
-- password: demo-password-2026 (stored as a {bcrypt} hash for Spring Security's delegating encoder).
--   System admin        admin@example.com
--   Organization owner  hannah.reid@example.com
--   Staff               maya.chen@example.com, daniel.brooks@example.com
--   Customers           alex.morgan@example.com, sofia.martin@example.com, james.wilson@example.com,
--                       olivia.bennett@example.com, ethan.parker@example.com
--
-- Appointments are generated relative to the day the seed runs: two weeks of history with
-- recorded service times (so Eq. 3 and Eq. 4 have data) and the next working week of bookings.
-- To regenerate them for a new demo day, reset the database: `make db-reset`.
--
-- Repeatable migration: Flyway re-runs it when this file changes, and the guard below makes it a
-- no-op when the demo organization already exists.

DO $$
DECLARE
    demo_password CONSTANT VARCHAR := '{bcrypt}$2a$10$odiChae1pJ4SbbV6c6CMTOIi3oi8y6AOYql6dUsj3GojeMPPrTWqa';
    zone CONSTANT VARCHAR := 'Europe/Istanbul';
    org BIGINT;
    advising BIGINT;
    documents BIGINT;
    maya_user BIGINT;
    daniel_user BIGINT;
    maya BIGINT;
    daniel BIGINT;
    customers BIGINT[];
BEGIN
    IF EXISTS (SELECT 1 FROM organization WHERE name = 'Northbridge University · Student Services') THEN
        RETURN;
    END IF;

    INSERT INTO organization (name, address, timezone, terminology)
    VALUES ('Northbridge University · Student Services', 'Student Centre, Floor 2', zone,
            '{"customer": "student", "staff": "advisor"}'::jsonb)
    RETURNING org_id INTO org;

    INSERT INTO app_user (org_id, full_name, email, password_hash, role)
    VALUES (NULL, 'System Admin', 'admin@example.com', demo_password, 'SYSTEM_ADMIN'),
           (org, 'Hannah Reid', 'hannah.reid@example.com', demo_password, 'ORGANIZATION_OWNER');

    INSERT INTO app_user (org_id, full_name, email, password_hash, role)
    VALUES (org, 'Maya Chen', 'maya.chen@example.com', demo_password, 'STAFF')
    RETURNING user_id INTO maya_user;
    INSERT INTO app_user (org_id, full_name, email, password_hash, role)
    VALUES (org, 'Daniel Brooks', 'daniel.brooks@example.com', demo_password, 'STAFF')
    RETURNING user_id INTO daniel_user;

    WITH inserted AS (
        INSERT INTO app_user (org_id, full_name, email, password_hash, role)
        VALUES (NULL, 'Alex Morgan', 'alex.morgan@example.com', demo_password, 'CUSTOMER'),
               (NULL, 'Sofia Martin', 'sofia.martin@example.com', demo_password, 'CUSTOMER'),
               (NULL, 'James Wilson', 'james.wilson@example.com', demo_password, 'CUSTOMER'),
               (NULL, 'Olivia Bennett', 'olivia.bennett@example.com', demo_password, 'CUSTOMER'),
               (NULL, 'Ethan Parker', 'ethan.parker@example.com', demo_password, 'CUSTOMER')
        RETURNING user_id)
    SELECT array_agg(user_id ORDER BY user_id) INTO customers FROM inserted;

    INSERT INTO staff (user_id, title, capacity) VALUES (maya_user, 'Academic advisor', 1)
    RETURNING staff_id INTO maya;
    INSERT INTO staff (user_id, title, capacity) VALUES (daniel_user, 'Senior advisor', 1)
    RETURNING staff_id INTO daniel;

    INSERT INTO service (org_id, name, description, expected_duration_min, learned_duration_min, buffer_min)
    VALUES (org, 'Academic advising', 'Course planning and registration', 30, 32.5, 0)
    RETURNING service_id INTO advising;
    INSERT INTO service (org_id, name, description, expected_duration_min, learned_duration_min, buffer_min)
    VALUES (org, 'Document request', 'Transcripts and enrolment letters', 15, 14.0, 0)
    RETURNING service_id INTO documents;

    INSERT INTO staff_service (staff_id, service_id)
    VALUES (maya, advising), (daniel, advising), (daniel, documents);

    INSERT INTO appointment_rule (service_id, max_advance_days, min_lead_hours, cancel_deadline_hours, max_active_per_user)
    VALUES (advising, 14, 2, 12, 2),
           (documents, 7, 1, 2, 1);

    -- Monday to Friday, 09:00-16:00 with a lunch break.
    INSERT INTO working_hours (staff_id, day_of_week, start_time, end_time, break_start, break_end)
    SELECT staff_member, weekday, TIME '09:00', TIME '16:00', TIME '12:00', TIME '13:00'
    FROM unnest(ARRAY[maya, daniel]) AS staff_member, generate_series(1, 5) AS weekday;

    -- History: the last 10 working days, five morning advising slots per staff member.
    -- Durations vary between 26 and 38 minutes and some services start late, so the learned
    -- duration (Eq. 3) and waiting estimates (Eq. 4) are not trivial. Every 11th slot is a
    -- no-show and every 13th was cancelled, for the analytics figures.
    INSERT INTO appointment (customer_id, staff_id, service_id, start_time, end_time, status,
                             booked_at, cancelled_at, checked_in_at, service_start_at, service_end_at)
    SELECT customers[1 + (slot.n % array_length(customers, 1))::int],
           slot.staff_member,
           advising,
           slot.starts_at,
           slot.starts_at + INTERVAL '30 minutes',
           CASE WHEN slot.n % 11 = 0 THEN 'NO_SHOW' WHEN slot.n % 13 = 0 THEN 'CANCELLED' ELSE 'COMPLETED' END,
           slot.starts_at - INTERVAL '3 days',
           CASE WHEN slot.n % 13 = 0 AND slot.n % 11 <> 0 THEN slot.starts_at - INTERVAL '1 day' END,
           CASE WHEN slot.n % 11 <> 0 AND slot.n % 13 <> 0 THEN slot.starts_at - INTERVAL '4 minutes' END,
           CASE WHEN slot.n % 11 <> 0 AND slot.n % 13 <> 0
                THEN slot.starts_at + make_interval(mins => ((slot.n * 7) % 9)::int) END,
           CASE WHEN slot.n % 11 <> 0 AND slot.n % 13 <> 0
                THEN slot.starts_at + make_interval(mins => ((slot.n * 7) % 9 + 26 + (slot.n * 5) % 13)::int) END
    FROM (
        SELECT row_number() OVER (ORDER BY day, slot_time, staff_member) AS n,
               staff_member,
               ((day + slot_time) AT TIME ZONE zone) AS starts_at
        FROM generate_series(1, 14) AS days_ago,
             LATERAL (SELECT current_date - days_ago AS day) AS calendar,
             unnest(ARRAY[TIME '09:00', TIME '09:30', TIME '10:00', TIME '10:30', TIME '11:00']) AS slot_time,
             unnest(ARRAY[maya, daniel]) AS staff_member
        WHERE extract(isodow FROM day) BETWEEN 1 AND 5
    ) AS slot;

    -- Ratings for most completed appointments: Maya averages about 4.7, Daniel about 4.8.
    INSERT INTO feedback (appt_id, rating, comment, created_at)
    SELECT appt_id,
           CASE WHEN staff_id = maya THEN 4 + ((appt_id % 10) > 2)::int ELSE 4 + ((appt_id % 10) > 1)::int END,
           CASE WHEN appt_id % 4 = 0 THEN 'Clear advice, thank you.' END,
           service_end_at + INTERVAL '2 hours'
    FROM appointment
    WHERE status = 'COMPLETED' AND appt_id % 3 <> 0;

    -- Upcoming: booked appointments over the next working week for both staff members.
    INSERT INTO appointment (customer_id, staff_id, service_id, start_time, end_time, status, booked_at)
    SELECT customers[1 + (slot.n % array_length(customers, 1))::int],
           slot.staff_member,
           advising,
           slot.starts_at,
           slot.starts_at + INTERVAL '30 minutes',
           'BOOKED',
           now() - INTERVAL '1 day'
    FROM (
        SELECT row_number() OVER (ORDER BY day, slot_time, staff_member) AS n,
               staff_member,
               ((day + slot_time) AT TIME ZONE zone) AS starts_at
        FROM generate_series(1, 7) AS days_ahead,
             LATERAL (SELECT current_date + days_ahead AS day) AS calendar,
             unnest(ARRAY[TIME '09:30', TIME '10:30', TIME '14:00']) AS slot_time,
             unnest(ARRAY[maya, daniel]) AS staff_member
        WHERE extract(isodow FROM day) BETWEEN 1 AND 5
    ) AS slot
    WHERE slot.n % 2 = 0 OR slot.n <= 4;
END
$$;
