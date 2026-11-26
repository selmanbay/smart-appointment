package com.smartappointment.estimation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlotsPerDayTest {

  private static final List<Duration> MORNING_AND_AFTERNOON =
      List.of(Duration.ofHours(3), Duration.ofHours(4));

  @Test
  void matchesTheSrsExample() {
    // SRS Eq. 1 example: 09:00-12:00 and 13:00-17:00, d = 20, b = 0, c = 1 give 9 + 12 = 21.
    int slots = SlotsPerDay.count(MORNING_AND_AFTERNOON, Duration.ofMinutes(20), Duration.ZERO, 1);

    assertThat(slots).isEqualTo(21);
  }

  @Test
  void bufferShortensEveryPeriodAndPartialSlotsDoNotCount() {
    int slots =
        SlotsPerDay.count(MORNING_AND_AFTERNOON, Duration.ofMinutes(20), Duration.ofMinutes(5), 1);

    assertThat(slots).isEqualTo(7 + 9);
  }

  @Test
  void capacityMultipliesTheSlots() {
    int slots = SlotsPerDay.count(MORNING_AND_AFTERNOON, Duration.ofMinutes(20), Duration.ZERO, 2);

    assertThat(slots).isEqualTo(42);
  }

  @Test
  void aDayWithoutWorkingHoursHasNoSlots() {
    assertThat(SlotsPerDay.count(List.of(), Duration.ofMinutes(20), Duration.ZERO, 1)).isZero();
  }

  @Test
  void rejectsAZeroSlotLength() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> SlotsPerDay.count(MORNING_AND_AFTERNOON, Duration.ZERO, Duration.ZERO, 1));
  }
}
