package com.smartappointment.estimation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class UtilisationTest {

  private static final Duration SEVEN_WORKING_HOURS = Duration.ofMinutes(420);

  @Test
  void matchesTheSrsExample() {
    // SRS Eq. 2 example: 17 booked 20-minute appointments in 420 minutes give 340/420, about 81 %.
    List<Duration> booked = Collections.nCopies(17, Duration.ofMinutes(20));

    assertThat(Utilisation.share(booked, SEVEN_WORKING_HOURS)).isCloseTo(0.81, within(0.005));
  }

  @Test
  void anEmptyDayIsNotUtilised() {
    assertThat(Utilisation.share(List.of(), SEVEN_WORKING_HOURS)).isZero();
  }

  @Test
  void rejectsADayWithoutAvailableTime() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> Utilisation.share(List.of(Duration.ofMinutes(20)), Duration.ZERO));
  }
}
