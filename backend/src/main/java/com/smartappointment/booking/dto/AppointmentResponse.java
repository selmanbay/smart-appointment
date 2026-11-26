package com.smartappointment.booking.dto;

import com.smartappointment.booking.AppointmentStatus;
import java.time.OffsetDateTime;

/**
 * One appointment (contract: Appointment). Times carry the organization's offset; {@code
 * estimatedWaitMinutes} is set only for BOOKED and CHECKED_IN appointments (SRS Eq. 4).
 */
public record AppointmentResponse(
    Long appointmentId,
    Long organizationId,
    ServiceReference service,
    StaffReference staff,
    CustomerReference customer,
    OffsetDateTime start,
    OffsetDateTime end,
    AppointmentStatus status,
    Long estimatedWaitMinutes,
    OffsetDateTime bookedAt,
    OffsetDateTime cancelledAt,
    OffsetDateTime checkedInAt,
    OffsetDateTime serviceStartAt,
    OffsetDateTime serviceEndAt,
    boolean canCancel,
    boolean hasFeedback) {

  /** The booked service. */
  public record ServiceReference(Long serviceId, String name) {}

  /** The staff member who serves the appointment. */
  public record StaffReference(Long staffId, String fullName) {}

  /** The customer who booked it. */
  public record CustomerReference(Long userId, String fullName) {}
}
