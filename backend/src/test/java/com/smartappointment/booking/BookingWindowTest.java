package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BookingWindowTest {

  private static final Instant NOW = Instant.parse("2026-11-26T07:00:00Z");

  @Test
  void withoutARuleAnyFutureStartIsAllowedButNoPastOne() {
    BookingWindow window = BookingWindow.forRule(NO_RULE, ISTANBUL, NOW);

    assertThat(window.allows(NOW)).isTrue();
    assertThat(window.allows(NOW.plus(Duration.ofDays(400)))).isTrue();
    assertThat(window.allows(NOW.minusSeconds(1))).isFalse();
  }

  @Test
  void theLeadTimeRejectsStartsTooSoon() {
    BookingWindow window =
        BookingWindow.forRule(new AppointmentRuleResponse(null, 2, null, null), ISTANBUL, NOW);

    assertThat(window.isLeadTimeMet(NOW.plus(Duration.ofMinutes(119)))).isFalse();
    assertThat(window.isLeadTimeMet(NOW.plus(Duration.ofHours(2)))).isTrue();
  }

  @Test
  void theAdvanceLimitCountsCalendarDaysInTheOrganizationsTimeZone() {
    // 22:30 UTC on 26 November is already 01:30 on 27 November in Istanbul.
    Instant lateEvening = Instant.parse("2026-11-26T22:30:00Z");
    BookingWindow window =
        BookingWindow.forRule(
            new AppointmentRuleResponse(1, null, null, null), ISTANBUL, lateEvening);

    assertThat(window.isWithinAdvanceLimit(Instant.parse("2026-11-28T20:59:00Z"))).isTrue();
    assertThat(window.isWithinAdvanceLimit(Instant.parse("2026-11-28T21:00:00Z"))).isFalse();
  }
}
