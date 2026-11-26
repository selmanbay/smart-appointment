package com.smartappointment.booking;

import com.smartappointment.auth.AccountService;
import com.smartappointment.common.web.ResourceNotFoundException;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Loads an {@link AppointmentChange}, locking the appointment row for the transaction. */
@Component
class AppointmentChangeLoader {

  private final AppointmentRepository appointmentRepository;
  private final BookingContextLoader bookingContextLoader;
  private final AccountService accountService;

  AppointmentChangeLoader(
      AppointmentRepository appointmentRepository,
      BookingContextLoader bookingContextLoader,
      AccountService accountService) {
    this.appointmentRepository = appointmentRepository;
    this.bookingContextLoader = bookingContextLoader;
    this.accountService = accountService;
  }

  /** 404 when the appointment does not exist. Needs a running transaction for the lock. */
  AppointmentChange load(Long appointmentId, Long callerUserId) {
    Appointment appointment =
        appointmentRepository
            .findLockedById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    return new AppointmentChange(
        appointment,
        bookingContextLoader.loadForAppointment(appointment),
        accountService.currentAccount(callerUserId));
  }

  /**
   * Where a reschedule wants to go: same service, the requested staff member and start; 409
   * STAFF_DOES_NOT_PROVIDE_SERVICE as for a booking.
   */
  BookingContext loadMoveTarget(Appointment appointment, Long newStaffId, Instant newStart) {
    return bookingContextLoader.loadForBooking(newStaffId, appointment.getServiceId(), newStart);
  }
}
