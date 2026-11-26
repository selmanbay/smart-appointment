package com.smartappointment.booking;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.smartappointment.booking.dto.SlotResponse;
import com.smartappointment.booking.dto.StaffOptionResponse;
import com.smartappointment.catalog.CatalogService;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.estimation.RankingCandidate;
import com.smartappointment.estimation.StaffRanking;
import com.smartappointment.feedback.FeedbackService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation listStaffRecommendations (UC-18, UC-19). Computed on every request from the
 * schedule and recorded service times, never stored (AGENT_RULES section 8).
 */
@Service
public class StaffRecommendationService {

  private final CatalogService catalogService;
  private final StaffLookupService staffLookupService;
  private final FeedbackService feedbackService;
  private final SlotService slotService;

  StaffRecommendationService(
      CatalogService catalogService,
      StaffLookupService staffLookupService,
      FeedbackService feedbackService,
      SlotService slotService) {
    this.catalogService = catalogService;
    this.staffLookupService = staffLookupService;
    this.feedbackService = feedbackService;
    this.slotService = slotService;
  }

  /** Throws 404 when the service is missing or inactive. */
  @Transactional(readOnly = true)
  public List<StaffOptionResponse> rankStaff(Long serviceId, LocalDate date) {
    catalogService.getActiveService(serviceId);
    Map<Long, StaffMemberSummary> staffById =
        staffLookupService.findActiveProviders(serviceId).stream()
            .collect(toMap(StaffMemberSummary::staffId, identity()));
    Map<Long, Double> ratingByStaffId = feedbackService.findMeanRatingByStaffId(staffById.keySet());
    Map<Long, StaffDay> dayByStaffId =
        staffById.keySet().stream()
            .collect(toMap(identity(), staffId -> slotService.staffDay(staffId, serviceId, date)));
    List<RankingCandidate> candidates =
        staffById.keySet().stream()
            .map(staffId ->
                new RankingCandidate(
                    staffId,
                    nextWaitMinutes(dayByStaffId.get(staffId)),
                    ratingByStaffId.get(staffId)))
            .toList();
    return StaffOptions.inRankOrder(StaffRanking.rank(candidates), staffById, dayByStaffId);
  }

  private static Long nextWaitMinutes(StaffDay day) {
    List<SlotResponse> freeSlots = day.freeSlots();
    return freeSlots.isEmpty() ? null : freeSlots.getFirst().estimatedWaitMinutes();
  }
}
