package com.smartappointment.booking;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.common.web.ForbiddenException;
import org.springframework.stereotype.Component;

/**
 * One appointment as a given account sees it (UC-6, UC-8): visible to its customer, its staff
 * member and the organization's owner, with the current estimated wait.
 */
@Component
class AppointmentViews {

  private final BookingContextLoader bookingContextLoader;
  private final AppointmentWaits appointmentWaits;
  private final AppointmentResponseMapper appointmentResponseMapper;

  AppointmentViews(
      BookingContextLoader bookingContextLoader,
      AppointmentWaits appointmentWaits,
      AppointmentResponseMapper appointmentResponseMapper) {
    this.bookingContextLoader = bookingContextLoader;
    this.appointmentWaits = appointmentWaits;
    this.appointmentResponseMapper = appointmentResponseMapper;
  }

  /** 403 when the viewer is neither the customer, the staff member nor the owner. */
  AppointmentResponse view(Appointment appointment, AccountResponse viewer) {
    BookingContext context = bookingContextLoader.loadForAppointment(appointment);
    if (!AccessRules.canView(appointment, viewer, context.service().organizationId())) {
      throw new ForbiddenException("This appointment belongs to someone else.");
    }
    return appointmentResponseMapper.toResponse(
        appointment, context, appointmentWaits.estimatedWaitMinutes(appointment, context), viewer);
  }
}
