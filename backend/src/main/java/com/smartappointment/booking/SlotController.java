package com.smartappointment.booking;

import com.smartappointment.booking.dto.SlotResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation listFreeSlots. */
@RestController
public class SlotController {

  private final SlotService slotService;

  public SlotController(SlotService slotService) {
    this.slotService = slotService;
  }

  @GetMapping("/api/v1/staff/{staffId}/slots")
  public List<SlotResponse> listFreeSlots(
      @PathVariable Long staffId,
      @RequestParam Long serviceId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return slotService.freeSlots(staffId, serviceId, date);
  }
}
