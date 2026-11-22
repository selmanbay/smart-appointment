package com.smartappointment.estimation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/** Updates a service's learned duration after a completed appointment (SRS Eq. 3, F9). */
public final class LearnedDuration {

  private static final BigDecimal ALPHA = new BigDecimal("0.2");
  private static final BigDecimal ONE_MINUS_ALPHA = new BigDecimal("0.8");
  private static final BigDecimal MINUTES_PER_SECOND_DIVISOR = BigDecimal.valueOf(60);
  private static final int DIVISION_SCALE = 10;

  private LearnedDuration() {}

  /**
   * SRS Eq. 3: {@code d_new = alpha * d_actual + (1 - alpha) * d_old}. When {@code
   * previousLearnedDurationMinutes} is {@code null} (no completed appointment yet), {@code d_old}
   * starts at {@code expectedDurationMinutes}.
   */
  public static BigDecimal next(
      BigDecimal previousLearnedDurationMinutes,
      int expectedDurationMinutes,
      Duration actualDuration) {
    BigDecimal previousDuration =
        previousLearnedDurationMinutes != null
            ? previousLearnedDurationMinutes
            : BigDecimal.valueOf(expectedDurationMinutes);
    BigDecimal actualMinutes = toMinutes(actualDuration);
    return ALPHA
        .multiply(actualMinutes)
        .add(ONE_MINUS_ALPHA.multiply(previousDuration))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private static BigDecimal toMinutes(Duration duration) {
    return BigDecimal.valueOf(duration.toSeconds())
        .divide(MINUTES_PER_SECOND_DIVISOR, DIVISION_SCALE, RoundingMode.HALF_UP);
  }
}
