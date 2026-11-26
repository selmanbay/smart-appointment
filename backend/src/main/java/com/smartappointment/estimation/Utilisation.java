package com.smartappointment.estimation;

import java.time.Duration;
import java.util.List;

/** The share of a staff member's working time that is booked (SRS Eq. 2, F10). */
public final class Utilisation {

  private Utilisation() {}

  /** SRS Eq. 2: {@code U = (sum of d_i) / T_avail}, as a fraction between 0 and 1 (or above). */
  public static double share(List<Duration> bookedDurations, Duration availableWorkingTime) {
    if (availableWorkingTime.isNegative() || availableWorkingTime.isZero()) {
      throw new IllegalArgumentException("available working time must be positive");
    }
    long bookedSeconds = bookedDurations.stream().mapToLong(Duration::toSeconds).sum();
    return (double) bookedSeconds / availableWorkingTime.toSeconds();
  }
}
