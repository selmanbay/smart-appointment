package com.smartappointment.booking;

/**
 * Lifecycle state of an {@link Appointment} (SRS Table 9, ck_appointment_status).
 *
 * <p>Allowed transitions (AGENT_RULES section 8): {@code BOOKED -> CHECKED_IN -> IN_SERVICE ->
 * COMPLETED}, and from {@code BOOKED} also {@code CANCELLED} or {@code NO_SHOW}.
 */
public enum AppointmentStatus {
  BOOKED,
  CHECKED_IN,
  IN_SERVICE,
  COMPLETED,
  CANCELLED,
  NO_SHOW
}
