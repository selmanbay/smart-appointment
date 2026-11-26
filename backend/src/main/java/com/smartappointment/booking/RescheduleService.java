package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.RescheduleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation rescheduleAppointment (UC-4, SDD Sequence 2). The same appointment row moves,
 * so it keeps its history; the new slot is checked like a booking and counted under the new staff
 * member's lock, all in one transaction. If any rule fails nothing changes: the original slot is
 * released only when the new one is taken. The customer is notified after commit (UC-20).
 */
@Service
public class RescheduleService {

  private final AppointmentChangeLoader appointmentChangeLoader;
  private final BookingRuleChecker bookingRuleChecker;
  private final BookingRecorder bookingRecorder;
  private final AppointmentResponseMapper appointmentResponseMapper;

  RescheduleService(
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
   * Only the appointment's customer, only a BOOKED appointment, only before {@code
   * cancel_deadline_hours} (checked before any lock, SDD Sequence 2). The service stays the same;
   * the staff member may change.
   */
  @Transactional
  public AppointmentResponse reschedule(
      Long appointmentId, Long callerUserId, RescheduleRequest request) {
    AppointmentChange change = appointmentChangeLoader.load(appointmentId, callerUserId);
    change.requireCustomer("reschedule");
    Appointment appointment = change.appointment();
    bookingRuleChecker.checkCustomerMayChange(appointment, change.context());

    BookingContext target =
        appointmentChangeLoader.loadMoveTarget(
            appointment, request.staffId(), request.start().toInstant());
    Slot slot = bookingRuleChecker.checkMove(target, request.start().toInstant());
    bookingRuleChecker.lockStaffMember(target);
    long estimatedWaitMinutes =
        bookingRuleChecker.checkCapacityForMove(target, slot, appointment.getAppointmentId());
    bookingRecorder.recordMove(appointment, target, slot);
    return appointmentResponseMapper.toResponse(appointment, target, estimatedWaitMinutes);
  }
}
