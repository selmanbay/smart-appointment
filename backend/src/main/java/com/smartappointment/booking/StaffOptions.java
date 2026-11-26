package com.smartappointment.booking;

import com.smartappointment.booking.dto.SlotResponse;
import com.smartappointment.booking.dto.StaffOptionResponse;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.estimation.RankingCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Turns ranked candidates into the contract's StaffOption list (UC-18). */
final class StaffOptions {

  /** Shown to the customer next to the first option (handoff B7, UC-18). */
  static final String RECOMMENDATION_REASON = "Shortest estimated wait";

  private StaffOptions() {}

  /**
   * The first option is recommended only if it has a free slot: with no free slot anywhere that
   * day, nobody is recommended.
   */
  static List<StaffOptionResponse> inRankOrder(
      List<RankingCandidate> ranked,
      Map<Long, StaffMemberSummary> staffById,
      Map<Long, StaffDay> dayByStaffId) {
    List<StaffOptionResponse> options = new ArrayList<>();
    for (RankingCandidate candidate : ranked) {
      boolean isRecommended = options.isEmpty() && candidate.hasFreeSlot();
      options.add(
          toOption(
              candidate,
              staffById.get(candidate.staffId()),
              dayByStaffId.get(candidate.staffId()),
              new Placement(options.size() + 1, isRecommended)));
    }
    return options;
  }

  private static StaffOptionResponse toOption(
      RankingCandidate candidate, StaffMemberSummary staff, StaffDay day, Placement placement) {
    SlotResponse nextFreeSlot = day.freeSlots().isEmpty() ? null : day.freeSlots().getFirst();
    return new StaffOptionResponse(
        staff.staffId(),
        staff.fullName(),
        staff.title(),
        placement.rank(),
        placement.isRecommended(),
        placement.isRecommended() ? RECOMMENDATION_REASON : null,
        nextFreeSlot == null ? null : nextFreeSlot.start(),
        nextFreeSlot == null ? 0 : nextFreeSlot.estimatedWaitMinutes(),
        day.bookedSlots(),
        day.totalSlots(),
        roundedRating(candidate.meanRating()));
  }

  private static Double roundedRating(Double meanRating) {
    return meanRating == null
        ? null
        : BigDecimal.valueOf(meanRating).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }

  private record Placement(int rank, boolean isRecommended) {}
}
