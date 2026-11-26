-- Idempotency-Key of the bookAppointment operation (api/openapi.yaml, ADR-0018): a customer who
-- repeats a booking request with the same key gets the first appointment back instead of a second one.

CREATE TABLE booking_idempotency_key (
    key_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     BIGINT      NOT NULL REFERENCES app_user (user_id),
    idempotency_key UUID        NOT NULL,
    appt_id         BIGINT      NOT NULL REFERENCES appointment (appt_id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_idempotency_key UNIQUE (customer_id, idempotency_key)
);
