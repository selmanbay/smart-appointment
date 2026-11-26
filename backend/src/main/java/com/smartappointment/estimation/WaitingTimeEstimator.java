package com.smartappointment.estimation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Projects start, finish and waiting time for a staff member's appointments (SRS Eq. 4, F9). */
public final class WaitingTimeEstimator {

  private WaitingTimeEstimator() {}

  /**
   * SRS Eq. 4: for appointments k = 1, 2, ... in start-time order, {@code S_k = max(s_k, F_(k-1))},
   * {@code F_k = S_k + d_k}, {@code W_k = S_k - s_k}; {@code F_0} is {@code openingTime} (now, or
   * the opening time for a future day). For {@code capacity > 1}, {@code F_(k-1)} is the earliest
   * projected finish among the parallel positions.
   */
  public static List<WaitingTimeEstimate> estimate(
      List<ScheduledService> appointmentsInStartOrder, int capacity, Instant openingTime) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be at least 1");
    }
    Instant[] positionFinishTimes = new Instant[capacity];
    Arrays.fill(positionFinishTimes, openingTime);

    List<WaitingTimeEstimate> estimates = new ArrayList<>();
    for (ScheduledService appointment : appointmentsInStartOrder) {
      int position = earliestPosition(positionFinishTimes);
      Instant projectedStart = max(appointment.scheduledStart(), positionFinishTimes[position]);
      Instant projectedFinish = projectedStart.plus(appointment.learnedDuration());
      Duration waitingTime = Duration.between(appointment.scheduledStart(), projectedStart);

      positionFinishTimes[position] = projectedFinish;
      estimates.add(new WaitingTimeEstimate(projectedStart, projectedFinish, waitingTime));
    }
    return estimates;
  }

  private static int earliestPosition(Instant[] positionFinishTimes) {
    int earliest = 0;
    for (int i = 1; i < positionFinishTimes.length; i++) {
      if (positionFinishTimes[i].isBefore(positionFinishTimes[earliest])) {
        earliest = i;
      }
    }
    return earliest;
  }

  private static Instant max(Instant left, Instant right) {
    return left.isAfter(right) ? left : right;
  }
}
