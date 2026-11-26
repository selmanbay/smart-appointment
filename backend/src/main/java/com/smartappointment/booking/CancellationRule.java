package com.smartappointment.booking;

import java.time.Duration;
import java.time.Instant;

/**
 * Whether a customer may still cancel or reschedule (SRS 2.5, UC-4, UC-5): only a BOOKED
 * appointment, and only until {@code cancel_deadline_hours} before its start (until the start when
 * the service has no deadline).
 */
final class CancellationRule {

  private CancellationRule() {}

  static boolean canCustomerCancel(
      Appointment appointment, Integer cancelDeadlineHours, Instant now) {
    if (appointment.getStatus() != AppointmentStatus.BOOKED) {
      return false;
    }
    Instant deadline =
        cancelDeadlineHours == null
            ? appointment.getStartTime()
            : appointment.getStartTime().minus(Duration.ofHours(cancelDeadlineHours));
    return now.isBefore(deadline);
  }
}
