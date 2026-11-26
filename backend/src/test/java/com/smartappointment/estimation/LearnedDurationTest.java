package com.smartappointment.estimation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class LearnedDurationTest {

  @Test
  void nextWeighsActualAgainstThePreviousLearnedDuration() {
    BigDecimal next = LearnedDuration.next(BigDecimal.valueOf(30), 30, Duration.ofMinutes(40));

    assertThat(next).isEqualByComparingTo("32.00");
  }

  @Test
  void nextStartsFromTheExpectedDurationWhenThereIsNoHistory() {
    BigDecimal next = LearnedDuration.next(null, 30, Duration.ofMinutes(20));

    assertThat(next).isEqualByComparingTo("28.00");
  }

  @Test
  void currentUsesTheLearnedDurationToTheSecond() {
    assertThat(LearnedDuration.current(new BigDecimal("32.50"), 30))
        .isEqualTo(Duration.ofSeconds(32 * 60 + 30));
  }

  @Test
  void currentFallsBackToTheExpectedDurationWithoutHistory() {
    assertThat(LearnedDuration.current(null, 30)).isEqualTo(Duration.ofMinutes(30));
  }
}
