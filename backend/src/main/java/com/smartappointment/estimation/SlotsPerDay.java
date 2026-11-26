package com.smartappointment.estimation;

import java.time.Duration;
import java.util.List;

/** How many appointments a staff member can take in one day (SRS Eq. 1, F4). */
public final class SlotsPerDay {

  private SlotsPerDay() {}

  /**
   * SRS Eq. 1: {@code N = sum over j of c * floor(L_j / (d + b))}, where {@code L_j} is each
   * continuous working period (working hours split by breaks).
   */
  public static int count(
      List<Duration> workingPeriods, Duration expectedDuration, Duration buffer, int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be at least 1");
    }
    Duration slotLength = expectedDuration.plus(buffer);
    if (slotLength.isNegative() || slotLength.isZero()) {
      throw new IllegalArgumentException("expected duration plus buffer must be positive");
    }
    long slotsPerPosition =
        workingPeriods.stream().mapToLong(period -> period.dividedBy(slotLength)).sum();
    return Math.toIntExact(slotsPerPosition * capacity);
  }
}
