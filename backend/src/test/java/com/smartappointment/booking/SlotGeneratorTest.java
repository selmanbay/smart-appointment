package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NINE_TO_FOUR_WITH_LUNCH;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.context;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.estimation.SlotsPerDay;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlotGeneratorTest {

  @Test
  void theBreakSplitsTheDayIntoTwoPeriods() {
    List<Slot> slots = SlotGenerator.generate(advising(1, NO_RULE), MONDAY);

    assertThat(slots).hasSize(12);
    assertThat(slots.getFirst().start()).isEqualTo(localTime(MONDAY, 9, 0));
    assertThat(slots.getLast().end()).isEqualTo(localTime(MONDAY, 16, 0));
    assertThat(slots)
        .noneMatch(slot -> slot.overlaps(localTime(MONDAY, 12, 0), localTime(MONDAY, 13, 0)))
        .anyMatch(slot -> slot.end().equals(localTime(MONDAY, 12, 0)))
        .anyMatch(slot -> slot.start().equals(localTime(MONDAY, 13, 0)));
  }

  @Test
  void theBufferFollowsEverySlotAndTheGridMatchesEq1() {
    List<Slot> slots = SlotGenerator.generate(context(20, 5, 1, NO_RULE), MONDAY);

    // Eq. 1: floor(180 / 25) = 7 slots in each three-hour period.
    int eq1Slots =
        SlotsPerDay.count(
            NINE_TO_FOUR_WITH_LUNCH.stream()
                .map(period -> Duration.between(period.start(), period.end()))
                .toList(),
            Duration.ofMinutes(20),
            Duration.ofMinutes(5),
            1);
    assertThat(slots).hasSize(eq1Slots).hasSize(14);
    assertThat(slots.get(1).start()).isEqualTo(localTime(MONDAY, 9, 25));
    assertThat(slots.get(0).end()).isEqualTo(localTime(MONDAY, 9, 20));
  }

  @Test
  void slotsAreGeneratedInTheOrganizationsTimeZone() {
    Instant firstStart = SlotGenerator.generate(advising(1, NO_RULE), MONDAY).getFirst().start();

    assertThat(firstStart).isEqualTo(Instant.parse("2026-11-30T06:00:00Z"));
  }

  @Test
  void aDayWithoutWorkingHoursHasNoSlots() {
    BookingContext dayOff =
        new BookingContext(
            SlotFixtures.STAFF_ID,
            advising(1, NO_RULE).service(),
            1,
            SlotFixtures.ISTANBUL,
            List.of(),
            NO_RULE);

    assertThat(SlotGenerator.generate(dayOff, MONDAY)).isEmpty();
  }
}
