package com.smartappointment.booking;

import com.smartappointment.auth.AccountService;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AppointmentNotice;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.common.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lets the notification feature read what a notice says without touching booking repositories. */
@Service
public class AppointmentNoticeService {

  private final AppointmentRepository appointmentRepository;
  private final BookingContextLoader bookingContextLoader;
  private final StaffLookupService staffLookupService;
  private final AccountService accountService;

  AppointmentNoticeService(
      AppointmentRepository appointmentRepository,
      BookingContextLoader bookingContextLoader,
      StaffLookupService staffLookupService,
      AccountService accountService) {
    this.appointmentRepository = appointmentRepository;
    this.bookingContextLoader = bookingContextLoader;
    this.staffLookupService = staffLookupService;
    this.accountService = accountService;
  }

  /** The appointment as it is now: after a reschedule, the new staff member and start. */
  @Transactional(readOnly = true)
  public AppointmentNotice noticeFor(Long appointmentId) {
    Appointment appointment =
        appointmentRepository
            .findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    BookingContext context = bookingContextLoader.loadForAppointment(appointment);
    AccountResponse customer = accountService.currentAccount(appointment.getCustomerId());
    String staffName =
        staffLookupService
            .findMemberSummary(appointment.getStaffId())
            .map(StaffMemberSummary::fullName)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", appointment.getStaffId()));
    return new AppointmentNotice(
        appointmentId,
        customer.email(),
        customer.fullName(),
        context.service().name(),
        staffName,
        appointment.getStartTime().atZone(context.zone()).toOffsetDateTime());
  }
}
