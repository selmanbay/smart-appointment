package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CancellationRuleTest {

  private static final Instant START = Instant.parse("2026-11-30T06:00:00Z");

  @Test
  void aBookedAppointmentCanBeCancelledUntilTheDeadline() {
    Appointment appointment = booked();

    assertThat(CancellationRule.canCustomerCancel(appointment, 12, START.minus(hours(13))))
        .isTrue();
    assertThat(CancellationRule.canCustomerCancel(appointment, 12, START.minus(hours(12))))
        .isFalse();
  }

  @Test
  void withoutADeadlineItCanBeCancelledUntilItStarts() {
    Appointment appointment = booked();

    assertThat(CancellationRule.canCustomerCancel(appointment, null, START.minusSeconds(1)))
        .isTrue();
    assertThat(CancellationRule.canCustomerCancel(appointment, null, START)).isFalse();
  }

  @Test
  void onlyABookedAppointmentCanBeCancelled() {
    Appointment checkedIn = booked();
    checkedIn.setStatus(AppointmentStatus.CHECKED_IN);

    assertThat(CancellationRule.canCustomerCancel(checkedIn, null, START.minus(hours(48))))
        .isFalse();
  }

  private static Appointment booked() {
    Appointment appointment = new Appointment(9L, 1L, 1L, START);
    appointment.setEndTime(START.plus(Duration.ofMinutes(30)));
    return appointment;
  }

  private static Duration hours(int hours) {
    return Duration.ofHours(hours);
  }
}
