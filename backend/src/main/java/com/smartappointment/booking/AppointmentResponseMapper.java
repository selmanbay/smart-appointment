package com.smartappointment.booking;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.AppointmentResponse.ServiceReference;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Builds the contract's Appointment view; times carry the organization's offset, and {@code
 * canCancel} depends on who is looking ({@link AccessRules#canCancel}).
 */
@Component
class AppointmentResponseMapper {

  private final AppointmentPartiesLoader appointmentPartiesLoader;
  private final Clock clock;

  AppointmentResponseMapper(AppointmentPartiesLoader appointmentPartiesLoader, Clock clock) {
    this.appointmentPartiesLoader = appointmentPartiesLoader;
    this.clock = clock;
  }

  /** As seen by the appointment's customer, who just booked, moved or cancelled it. */
  AppointmentResponse toResponse(
      Appointment appointment, BookingContext context, Long estimatedWaitMinutes) {
    boolean canCancel =
        CancellationRule.canCustomerCancel(
            appointment, context.rule().cancelDeadlineHours(), clock.instant());
    return build(appointment, context, estimatedWaitMinutes, canCancel);
  }

  /** As seen by {@code viewer}. {@code estimatedWaitMinutes} may be null. */
  AppointmentResponse toResponse(
      Appointment appointment,
      BookingContext context,
      Long estimatedWaitMinutes,
      AccountResponse viewer) {
    boolean canCancel = AccessRules.canCancel(appointment, context, viewer, clock.instant());
    return build(appointment, context, estimatedWaitMinutes, canCancel);
  }

  /** {@code estimatedWaitMinutes} is dropped unless BOOKED or CHECKED_IN. */
  private AppointmentResponse build(
      Appointment appointment,
      BookingContext context,
      Long estimatedWaitMinutes,
      boolean canCancel) {
    ZoneId zone = context.zone();
    AppointmentParties parties = appointmentPartiesLoader.load(appointment);
    return new AppointmentResponse(
        appointment.getAppointmentId(),
        context.service().organizationId(),
        new ServiceReference(context.service().serviceId(), context.service().name()),
        parties.staff(),
        parties.customer(),
        inZone(appointment.getStartTime(), zone),
        inZone(appointment.getEndTime(), zone),
        appointment.getStatus(),
        appointment.getStatus().isWaitingForService() ? estimatedWaitMinutes : null,
        inZone(appointment.getBookedAt(), zone),
        inZone(appointment.getCancelledAt(), zone),
        inZone(appointment.getCheckedInAt(), zone),
        inZone(appointment.getServiceStartAt(), zone),
        inZone(appointment.getServiceEndAt(), zone),
        canCancel,
        parties.hasFeedback());
  }

  private static OffsetDateTime inZone(Instant instant, ZoneId zone) {
    return instant == null ? null : instant.atZone(zone).toOffsetDateTime();
  }
}
