package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.THURSDAY;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.bookedAt;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static com.smartappointment.booking.SlotFixtures.nothingBooked;
import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.booking.dto.SlotResponse;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FreeSlotsTest {

  private static final Instant THURSDAY_TEN = localTime(THURSDAY, 10, 0);

  @Test
  void anEmptyDayOffersEverySlotWithoutWait() {
    List<SlotResponse> slots = select(advising(1, NO_RULE), nothingBooked(), MONDAY, THURSDAY_TEN);

    assertThat(slots).hasSize(12);
    assertThat(slots.getFirst().start()).isEqualTo(OffsetDateTime.parse("2026-11-30T09:00+03:00"));
    assertThat(slots.getFirst().end()).isEqualTo(OffsetDateTime.parse("2026-11-30T09:30+03:00"));
    assertThat(slots).allMatch(slot -> slot.remainingCapacity() == 1);
    assertThat(slots).allMatch(slot -> slot.estimatedWaitMinutes() == 0);
  }

  @Test
  void aFullSlotIsLeftOut() {
    List<SlotResponse> slots =
        select(advising(1, NO_RULE), bookedAt(localTime(MONDAY, 9, 30), 30), MONDAY, THURSDAY_TEN);

    assertThat(slots).hasSize(11);
    assertThat(slots).noneMatch(slot -> slot.start().toInstant().equals(localTime(MONDAY, 9, 30)));
  }

  @Test
  void capacityTwoWithOneBookingLeavesOnePlace() {
    List<SlotResponse> slots =
        select(advising(2, NO_RULE), bookedAt(localTime(MONDAY, 9, 0), 30), MONDAY, THURSDAY_TEN);

    assertThat(slots).hasSize(12);
    assertThat(slots.get(0).remainingCapacity()).isEqualTo(1);
    assertThat(slots.get(1).remainingCapacity()).isEqualTo(2);
  }

  @Test
  void aLateRunPushesTheEstimatedWaitOfTheNextSlot() {
    // The 09:00 booking is planned for 30 minutes, but the service has been taking 45.
    List<SlotResponse> slots =
        select(advising(1, NO_RULE), bookedAt(localTime(MONDAY, 9, 0), 45), MONDAY, THURSDAY_TEN);

    assertThat(slots.getFirst().start().toInstant()).isEqualTo(localTime(MONDAY, 9, 30));
    assertThat(slots.getFirst().estimatedWaitMinutes()).isEqualTo(15);
    assertThat(slots.get(1).estimatedWaitMinutes()).isZero();
  }

  @Test
  void todayABookingStillWaitingDelaysTheNextSlotFromNow() {
    // SRS Eq. 4: F_0 is now. The 09:30 customer has not been served at 10:10, so they are
    // projected to run 10:10-10:40 and the 10:30 slot waits 10 minutes.
    Instant tenPastTen = localTime(THURSDAY, 10, 10);
    List<SlotResponse> slots =
        select(
            advising(1, NO_RULE), bookedAt(localTime(THURSDAY, 9, 30), 30), THURSDAY, tenPastTen);

    assertThat(slots.getFirst().start().toInstant()).isEqualTo(localTime(THURSDAY, 10, 30));
    assertThat(slots.getFirst().estimatedWaitMinutes()).isEqualTo(10);
  }

  @Test
  void theLeadTimeHidesSlotsThatStartTooSoon() {
    AppointmentRuleResponse twoHoursAhead = new AppointmentRuleResponse(null, 2, null, null);

    List<SlotResponse> slots =
        select(advising(1, twoHoursAhead), nothingBooked(), THURSDAY, THURSDAY_TEN);

    assertThat(slots.getFirst().start().toInstant()).isEqualTo(localTime(THURSDAY, 13, 0));
  }

  @Test
  void slotsAlreadyStartedTodayAreNeverOffered() {
    List<SlotResponse> slots =
        select(advising(1, NO_RULE), nothingBooked(), THURSDAY, THURSDAY_TEN);

    assertThat(slots.getFirst().start().toInstant()).isEqualTo(THURSDAY_TEN);
  }

  @Test
  void theAdvanceLimitHidesDaysTooFarAhead() {
    AppointmentRuleResponse threeDays = new AppointmentRuleResponse(3, null, null, null);
    AppointmentRuleResponse fourDays = new AppointmentRuleResponse(4, null, null, null);

    assertThat(select(advising(1, threeDays), nothingBooked(), MONDAY, THURSDAY_TEN)).isEmpty();
    assertThat(select(advising(1, fourDays), nothingBooked(), MONDAY, THURSDAY_TEN)).hasSize(12);
  }

  private static List<SlotResponse> select(
      BookingContext context, BookedDay bookedDay, LocalDate date, Instant now) {
    return FreeSlots.select(SlotGenerator.generate(context, date), context, bookedDay, now);
  }
}
