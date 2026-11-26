package com.smartappointment.booking;

import com.smartappointment.booking.dto.AgendaResponse;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation getStaffAgenda. The principal name is the token subject (user id). */
@RestController
public class AgendaController {

  private final AgendaService agendaService;

  public AgendaController(AgendaService agendaService) {
    this.agendaService = agendaService;
  }

  @GetMapping("/api/v1/staff/{staffId}/agenda")
  public AgendaResponse getStaffAgenda(
      @PathVariable Long staffId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      Principal principal) {
    return agendaService.getAgenda(staffId, date, Long.valueOf(principal.getName()));
  }
}
