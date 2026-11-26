package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.RescheduleRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract operations rescheduleAppointment and cancelAppointment. The principal name is the token
 * subject (user id).
 */
@RestController
@RequestMapping("/api/v1/appointments/{appointmentId}")
public class AppointmentChangeController {

  private final RescheduleService rescheduleService;
  private final CancellationService cancellationService;

  public AppointmentChangeController(
      RescheduleService rescheduleService, CancellationService cancellationService) {
    this.rescheduleService = rescheduleService;
    this.cancellationService = cancellationService;
  }

  @PostMapping("/reschedule")
  public AppointmentResponse rescheduleAppointment(
      @PathVariable Long appointmentId,
      Principal principal,
      @Valid @RequestBody RescheduleRequest request) {
    return rescheduleService.reschedule(appointmentId, Long.valueOf(principal.getName()), request);
  }

  /**
   * The contract's optional {@code reason} is not read: the schema has no column for it (open
   * question in docs/features/booking.md).
   */
  @PostMapping("/cancel")
  public AppointmentResponse cancelAppointment(
      @PathVariable Long appointmentId, Principal principal) {
    return cancellationService.cancel(appointmentId, Long.valueOf(principal.getName()));
  }
}
