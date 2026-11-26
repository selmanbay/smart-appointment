package com.smartappointment.booking;

import com.smartappointment.booking.dto.StaffOptionResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation listStaffRecommendations. */
@RestController
public class StaffRecommendationController {

  private final StaffRecommendationService staffRecommendationService;

  public StaffRecommendationController(StaffRecommendationService staffRecommendationService) {
    this.staffRecommendationService = staffRecommendationService;
  }

  @GetMapping("/api/v1/services/{serviceId}/staff-recommendations")
  public List<StaffOptionResponse> listStaffRecommendations(
      @PathVariable Long serviceId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return staffRecommendationService.rankStaff(serviceId, date);
  }
}
