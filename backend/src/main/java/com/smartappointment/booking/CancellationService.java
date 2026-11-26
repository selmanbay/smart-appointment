package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.common.web.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation cancelAppointment (UC-5, UC-14). The appointment is marked CANCELLED, never
 * deleted (AGENT_RULES section 8), and the customer is notified after commit (UC-20).
 */
@Service
public class CancellationService {

  private final AppointmentChangeLoader appointmentChangeLoader;
  private final BookingRuleChecker bookingRuleChecker;
  private final BookingRecorder bookingRecorder;
  private final AppointmentResponseMapper appointmentResponseMapper;

  CancellationService(
      AppointmentChangeLoader appointmentChangeLoader,
      BookingRuleChecker bookingRuleChecker,
      BookingRecorder bookingRecorder,
      AppointmentResponseMapper appointmentResponseMapper) {
    this.appointmentChangeLoader = appointmentChangeLoader;
    this.bookingRuleChecker = bookingRuleChecker;
    this.bookingRecorder = bookingRecorder;
    this.appointmentResponseMapper = appointmentResponseMapper;
  }

  /**
   * The organization's owner may cancel any BOOKED appointment; its customer only until {@code
   * cancel_deadline_hours} before the start (409 CANCEL_DEADLINE_PASSED). Anyone else gets 403.
   */
  @Transactional
  public AppointmentResponse cancel(Long appointmentId, Long callerUserId) {
    AppointmentChange change = appointmentChangeLoader.load(appointmentId, callerUserId);
    Appointment appointment = change.appointment();
    if (change.isOrganizationOwner()) {
      BookingProblem.INVALID_STATUS.unless(appointment.getStatus() == AppointmentStatus.BOOKED);
    } else if (change.isCustomer()) {
      bookingRuleChecker.checkCustomerMayChange(appointment, change.context());
    } else {
      throw new ForbiddenException("Only the customer or the organization's owner can cancel.");
    }
    bookingRecorder.recordCancellation(appointment);
    return appointmentResponseMapper.toResponse(
        appointment, change.context(), null, change.caller());
  }
}
