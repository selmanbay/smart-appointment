package com.smartappointment.estimation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class WaitingTimeEstimatorTest {

  private static final Instant OPENING_TIME = Instant.parse("2026-11-23T09:00:00Z");

  @Test
  void anEmptyDayHasNoEstimates() {
    List<WaitingTimeEstimate> estimates = WaitingTimeEstimator.estimate(List.of(), 1, OPENING_TIME);

    assertThat(estimates).isEmpty();
  }

  @Test
  void aRunningLateAppointmentPushesTheNextOne() {
    ScheduledService first = new ScheduledService(OPENING_TIME, Duration.ofMinutes(30));
    ScheduledService second =
        new ScheduledService(OPENING_TIME.plusSeconds(20 * 60), Duration.ofMinutes(20));

    List<WaitingTimeEstimate> estimates =
        WaitingTimeEstimator.estimate(List.of(first, second), 1, OPENING_TIME);

    WaitingTimeEstimate firstEstimate = estimates.get(0);
    assertThat(firstEstimate.projectedStart()).isEqualTo(OPENING_TIME);
    assertThat(firstEstimate.waitingTime()).isZero();

    WaitingTimeEstimate secondEstimate = estimates.get(1);
    assertThat(secondEstimate.projectedStart()).isEqualTo(OPENING_TIME.plusSeconds(30 * 60));
    assertThat(secondEstimate.waitingTime()).isEqualTo(Duration.ofMinutes(10));
  }

  @Test
  void capacityTwoAssignsEachAppointmentToTheEarliestFreePosition() {
    ScheduledService firstAtNine = new ScheduledService(OPENING_TIME, Duration.ofMinutes(30));
    ScheduledService secondAtNine = new ScheduledService(OPENING_TIME, Duration.ofMinutes(30));
    ScheduledService thirdTenMinutesLater =
        new ScheduledService(OPENING_TIME.plusSeconds(10 * 60), Duration.ofMinutes(20));

    List<WaitingTimeEstimate> estimates =
        WaitingTimeEstimator.estimate(
            List.of(firstAtNine, secondAtNine, thirdTenMinutesLater), 2, OPENING_TIME);

    assertThat(estimates.get(0).waitingTime()).isZero();
    assertThat(estimates.get(1).waitingTime()).isZero();

    WaitingTimeEstimate third = estimates.get(2);
    assertThat(third.projectedStart()).isEqualTo(OPENING_TIME.plusSeconds(30 * 60));
    assertThat(third.waitingTime()).isEqualTo(Duration.ofMinutes(20));
  }
}
