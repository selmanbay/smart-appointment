-- Initial schema from SRS section 2.5 (Figure 8, Table 9 and the integrity constraints).
-- Times are stored in UTC (timestamptz). Records are deactivated, never deleted.

CREATE TABLE organization (
    org_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    address     VARCHAR(500),
    timezone    VARCHAR(64)  NOT NULL DEFAULT 'UTC',
    logo        VARCHAR(500),
    terminology JSONB        NOT NULL DEFAULT '{}'::jsonb,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- "user" is a reserved word in PostgreSQL.
CREATE TABLE app_user (
    user_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id        BIGINT REFERENCES organization (org_id),
    full_name     VARCHAR(200) NOT NULL,
    email         VARCHAR(320) NOT NULL,
    phone         VARCHAR(32),
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    fcm_token     VARCHAR(500),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_user_email UNIQUE (email),
    CONSTRAINT ck_app_user_role CHECK (role IN ('SYSTEM_ADMIN', 'ORGANIZATION_OWNER', 'STAFF', 'CUSTOMER')),
    CONSTRAINT ck_app_user_org_required CHECK (role IN ('SYSTEM_ADMIN', 'CUSTOMER') OR org_id IS NOT NULL)
);

CREATE TABLE staff (
    staff_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   BIGINT      NOT NULL REFERENCES app_user (user_id),
    title     VARCHAR(100),
    capacity  INTEGER     NOT NULL DEFAULT 1,
    is_active BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_staff_user UNIQUE (user_id),
    CONSTRAINT ck_staff_capacity CHECK (capacity >= 1)
);

CREATE TABLE service (
    service_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id                BIGINT       NOT NULL REFERENCES organization (org_id),
    name                  VARCHAR(200) NOT NULL,
    description           TEXT,
    expected_duration_min INTEGER      NOT NULL,
    learned_duration_min  NUMERIC(7, 2),
    buffer_min            INTEGER      NOT NULL DEFAULT 0,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_service_expected_duration CHECK (expected_duration_min > 0),
    CONSTRAINT ck_service_learned_duration CHECK (learned_duration_min IS NULL OR learned_duration_min > 0),
    CONSTRAINT ck_service_buffer CHECK (buffer_min >= 0)
);

-- M:N "provides" relationship between staff and services.
CREATE TABLE staff_service (
    staff_id   BIGINT NOT NULL REFERENCES staff (staff_id),
    service_id BIGINT NOT NULL REFERENCES service (service_id),
    PRIMARY KEY (staff_id, service_id)
);

CREATE TABLE appointment_rule (
    rule_id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service_id            BIGINT  NOT NULL REFERENCES service (service_id),
    max_advance_days      INTEGER,
    min_lead_hours        INTEGER,
    cancel_deadline_hours INTEGER,
    max_active_per_user   INTEGER,
    CONSTRAINT uq_appointment_rule_service UNIQUE (service_id),
    CONSTRAINT ck_appointment_rule_values CHECK (
        COALESCE(max_advance_days, 0) >= 0 AND COALESCE(min_lead_hours, 0) >= 0
        AND COALESCE(cancel_deadline_hours, 0) >= 0 AND COALESCE(max_active_per_user, 1) >= 1)
);

CREATE TABLE working_hours (
    wh_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id    BIGINT   NOT NULL REFERENCES staff (staff_id),
    day_of_week SMALLINT NOT NULL,
    start_time  TIME     NOT NULL,
    end_time    TIME     NOT NULL,
    break_start TIME,
    break_end   TIME,
    CONSTRAINT ck_working_hours_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_working_hours_range CHECK (start_time < end_time),
    CONSTRAINT ck_working_hours_break CHECK (
        (break_start IS NULL AND break_end IS NULL)
        OR (break_start < break_end AND break_start >= start_time AND break_end <= end_time))
);

CREATE TABLE schedule_change_request (
    request_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id       BIGINT      NOT NULL REFERENCES staff (staff_id),
    day_of_week    SMALLINT    NOT NULL,
    new_start_time TIME        NOT NULL,
    new_end_time   TIME        NOT NULL,
    requested_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    decided_at     TIMESTAMPTZ,
    CONSTRAINT ck_schedule_change_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_schedule_change_range CHECK (new_start_time < new_end_time),
    CONSTRAINT ck_schedule_change_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE TABLE appointment (
    appt_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id      BIGINT      NOT NULL REFERENCES app_user (user_id),
    staff_id         BIGINT      NOT NULL REFERENCES staff (staff_id),
    service_id       BIGINT      NOT NULL REFERENCES service (service_id),
    start_time       TIMESTAMPTZ NOT NULL,
    end_time         TIMESTAMPTZ NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    booked_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    cancelled_at     TIMESTAMPTZ,
    checked_in_at    TIMESTAMPTZ,
    service_start_at TIMESTAMPTZ,
    service_end_at   TIMESTAMPTZ,
    CONSTRAINT ck_appointment_range CHECK (start_time < end_time),
    CONSTRAINT ck_appointment_status CHECK (
        status IN ('BOOKED', 'CHECKED_IN', 'IN_SERVICE', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT ck_appointment_service_times CHECK (
        service_end_at IS NULL OR (service_start_at IS NOT NULL AND service_start_at <= service_end_at))
);

CREATE INDEX ix_appointment_staff_start ON appointment (staff_id, start_time);
CREATE INDEX ix_appointment_customer_start ON appointment (customer_id, start_time);

CREATE TABLE notification (
    notif_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appt_id           BIGINT      NOT NULL REFERENCES appointment (appt_id),
    channel           VARCHAR(10) NOT NULL,
    type              VARCHAR(30) NOT NULL,
    scheduled_for     TIMESTAMPTZ NOT NULL,
    sent_at           TIMESTAMPTZ,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_response VARCHAR(1000),
    CONSTRAINT ck_notification_channel CHECK (channel IN ('EMAIL', 'PUSH')),
    CONSTRAINT ck_notification_type CHECK (
        type IN ('CONFIRMATION', 'REMINDER', 'CANCELLATION', 'RESCHEDULE')),
    CONSTRAINT ck_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX ix_notification_pending ON notification (scheduled_for) WHERE status = 'PENDING';

CREATE TABLE feedback (
    feedback_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appt_id     BIGINT      NOT NULL REFERENCES appointment (appt_id),
    rating      SMALLINT    NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_feedback_appointment UNIQUE (appt_id),
    CONSTRAINT ck_feedback_rating CHECK (rating BETWEEN 1 AND 5)
);
