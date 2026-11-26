package com.smartappointment.booking;

import com.smartappointment.auth.AccountService;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.common.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contract operation getAppointment (UC-6, UC-8). */
@Service
public class AppointmentQueryService {

  private final AppointmentRepository appointmentRepository;
  private final AccountService accountService;
  private final AppointmentViews appointmentViews;

  AppointmentQueryService(
      AppointmentRepository appointmentRepository,
      AccountService accountService,
      AppointmentViews appointmentViews) {
    this.appointmentRepository = appointmentRepository;
    this.accountService = accountService;
    this.appointmentViews = appointmentViews;
  }

  /** 404 when missing; 403 unless the viewer is its customer, its staff member or the owner. */
  @Transactional(readOnly = true)
  public AppointmentResponse getAppointment(Long appointmentId, Long viewerUserId) {
    Appointment appointment =
        appointmentRepository
            .findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    return appointmentViews.view(appointment, accountService.currentAccount(viewerUserId));
  }
}
