package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentPageResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.AppointmentScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract operations getAppointment and listMyAppointments. The principal name is the token
 * subject (user id).
 */
@RestController
public class AppointmentQueryController {

  private static final int MAX_PAGE_SIZE = 100;

  private final AppointmentQueryService appointmentQueryService;
  private final MyAppointmentsService myAppointmentsService;

  public AppointmentQueryController(
      AppointmentQueryService appointmentQueryService,
      MyAppointmentsService myAppointmentsService) {
    this.appointmentQueryService = appointmentQueryService;
    this.myAppointmentsService = myAppointmentsService;
  }

  @GetMapping("/api/v1/appointments/{appointmentId}")
  public AppointmentResponse getAppointment(@PathVariable Long appointmentId, Principal principal) {
    return appointmentQueryService.getAppointment(appointmentId, Long.valueOf(principal.getName()));
  }

  @GetMapping("/api/v1/me/appointments")
  public AppointmentPageResponse listMyAppointments(
      Principal principal,
      @RequestParam(defaultValue = "UPCOMING") AppointmentScope scope,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
    return myAppointmentsService.listMyAppointments(
        Long.valueOf(principal.getName()), scope, page, size);
  }
}
