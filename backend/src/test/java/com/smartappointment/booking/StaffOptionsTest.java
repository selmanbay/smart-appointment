package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.booking.dto.SlotResponse;
import com.smartappointment.booking.dto.StaffOptionResponse;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.estimation.RankingCandidate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StaffOptionsTest {

  private static final StaffMemberSummary MAYA =
      new StaffMemberSummary(1L, "Maya Chen", "Academic advisor", 1);
  private static final StaffMemberSummary DANIEL =
      new StaffMemberSummary(2L, "Daniel Brooks", "Senior advisor", 1);
  private static final OffsetDateTime NINE_THIRTY = OffsetDateTime.parse("2026-11-30T09:30+03:00");

  @Test
  void theFirstOptionIsRecommendedWithItsReason() {
    List<StaffOptionResponse> options =
        StaffOptions.inRankOrder(
            List.of(new RankingCandidate(2, 5L, 4.84), new RankingCandidate(1, 15L, null)),
            Map.of(1L, MAYA, 2L, DANIEL),
            Map.of(1L, dayWithFreeSlot(15), 2L, dayWithFreeSlot(5)));

    StaffOptionResponse first = options.get(0);
    assertThat(first.fullName()).isEqualTo("Daniel Brooks");
    assertThat(first.rank()).isEqualTo(1);
    assertThat(first.isRecommended()).isTrue();
    assertThat(first.recommendationReason()).isEqualTo("Shortest estimated wait");
    assertThat(first.nextFreeSlot()).isEqualTo(NINE_THIRTY);
    assertThat(first.estimatedWaitMinutes()).isEqualTo(5);
    assertThat(first.bookedSlots()).isEqualTo(3);
    assertThat(first.totalSlots()).isEqualTo(12);
    assertThat(first.meanRating()).isEqualTo(4.8);

    StaffOptionResponse second = options.get(1);
    assertThat(second.rank()).isEqualTo(2);
    assertThat(second.isRecommended()).isFalse();
    assertThat(second.recommendationReason()).isNull();
    assertThat(second.meanRating()).isNull();
  }

  @Test
  void nobodyIsRecommendedWhenNoStaffMemberHasAFreeSlot() {
    List<StaffOptionResponse> options =
        StaffOptions.inRankOrder(
            List.of(new RankingCandidate(1, null, 4.7)),
            Map.of(1L, MAYA),
            Map.of(1L, new StaffDay(List.of(), 12, 12)));

    StaffOptionResponse only = options.getFirst();
    assertThat(only.isRecommended()).isFalse();
    assertThat(only.recommendationReason()).isNull();
    assertThat(only.nextFreeSlot()).isNull();
    assertThat(only.estimatedWaitMinutes()).isZero();
    assertThat(only.bookedSlots()).isEqualTo(12);
  }

  private static StaffDay dayWithFreeSlot(long estimatedWaitMinutes) {
    SlotResponse slot =
        new SlotResponse(NINE_THIRTY, NINE_THIRTY.plusMinutes(30), 1, estimatedWaitMinutes);
    return new StaffDay(List.of(slot), 12, 3);
  }
}
