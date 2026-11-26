package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.BookingRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation bookAppointment. The principal name is the token subject (user id). */
@RestController
public class BookingController {

  private static final String APPOINTMENTS_PATH = "/api/v1/appointments";

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping(APPOINTMENTS_PATH)
  public ResponseEntity<AppointmentResponse> bookAppointment(
      Principal principal,
      @Valid @RequestBody BookingRequest request,
      @RequestHeader(name = "Idempotency-Key", required = false) UUID idempotencyKey) {
    AppointmentResponse appointment =
        bookingService.book(Long.valueOf(principal.getName()), request, idempotencyKey);
    URI location = URI.create(APPOINTMENTS_PATH + "/" + appointment.appointmentId());
    return ResponseEntity.created(location).body(appointment);
  }
}
