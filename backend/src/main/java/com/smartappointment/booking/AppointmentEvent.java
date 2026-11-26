package com.smartappointment.booking;

/**
 * Published when a customer's appointment is booked, moved or cancelled (UC-20). Listeners run
 * after the transaction commits, so a failed e-mail cannot undo the change (AGENT_RULES section 8).
 * The event lives in booking so that the notification feature depends on booking and not the other
 * way round.
 */
public record AppointmentEvent(Long appointmentId, Change change) {

  /** What happened to the appointment. */
  public enum Change {
    BOOKED,
    RESCHEDULED,
    CANCELLED
  }
}
