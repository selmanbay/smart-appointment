package com.smartappointment.booking;

import java.util.EnumSet;
import java.util.Set;

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
  NO_SHOW;

  /** Statuses that still take a place in the staff member's capacity (SRS 2.5). */
  public static final Set<AppointmentStatus> HOLDING_CAPACITY =
      EnumSet.of(BOOKED, CHECKED_IN, IN_SERVICE);

  /** Statuses that count as a booked slot in a staff member's workload. */
  public static final Set<AppointmentStatus> NOT_CANCELLED =
      EnumSet.complementOf(EnumSet.of(CANCELLED));

  /** The customer has not been served yet, so an estimated wait applies (contract: Appointment). */
  public boolean isWaitingForService() {
    return this == BOOKED || this == CHECKED_IN;
  }
}
