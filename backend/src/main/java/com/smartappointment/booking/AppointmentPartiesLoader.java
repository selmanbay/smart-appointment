package com.smartappointment.booking;

import com.smartappointment.auth.AccountService;
import com.smartappointment.booking.dto.AppointmentResponse.CustomerReference;
import com.smartappointment.booking.dto.AppointmentResponse.StaffReference;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.feedback.FeedbackService;
import org.springframework.stereotype.Component;

/** Reads the staff and customer names (catalog, auth) and the feedback flag of an appointment. */
@Component
class AppointmentPartiesLoader {

  private final StaffLookupService staffLookupService;
  private final AccountService accountService;
  private final FeedbackService feedbackService;

  AppointmentPartiesLoader(
      StaffLookupService staffLookupService,
      AccountService accountService,
      FeedbackService feedbackService) {
    this.staffLookupService = staffLookupService;
    this.accountService = accountService;
    this.feedbackService = feedbackService;
  }

  AppointmentParties load(Appointment appointment) {
    StaffMemberSummary staff =
        staffLookupService
            .findMemberSummary(appointment.getStaffId())
            .orElseThrow(() -> new ResourceNotFoundException("Staff", appointment.getStaffId()));
    String customerName = accountService.currentAccount(appointment.getCustomerId()).fullName();
    return new AppointmentParties(
        new StaffReference(staff.staffId(), staff.fullName()),
        new CustomerReference(appointment.getCustomerId(), customerName),
        feedbackService.hasFeedback(appointment.getAppointmentId()));
  }
}
