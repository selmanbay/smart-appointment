package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.StatusChangeRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation changeAppointmentStatus. The principal name is the token subject. */
@RestController
public class AppointmentStatusController {

  private final AppointmentStatusService appointmentStatusService;

  public AppointmentStatusController(AppointmentStatusService appointmentStatusService) {
    this.appointmentStatusService = appointmentStatusService;
  }

  @PostMapping("/api/v1/appointments/{appointmentId}/status")
  public AppointmentResponse changeAppointmentStatus(
      @PathVariable Long appointmentId,
      Principal principal,
      @Valid @RequestBody StatusChangeRequest request) {
    return appointmentStatusService.changeStatus(
        appointmentId,
        Long.valueOf(principal.getName()),
        AppointmentStatus.valueOf(request.status().name()));
  }
}
