package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.estimation.ScheduledService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BookedDayTest {

  private static final Instant NINE = Instant.parse("2026-11-30T06:00:00Z");
  private static final Instant TEN = NINE.plus(Duration.ofHours(1));

  @Test
  void withoutDropsTheAppointmentAndItsQueueEntry() {
    BookedDay day =
        new BookedDay(
            List.of(appointment(1L, NINE), appointment(2L, TEN)),
            List.of(queued(NINE, 30), queued(TEN, 45)),
            3);

    BookedDay withoutFirst = day.without(1L);

    assertThat(withoutFirst.holdingCapacity())
        .extracting(Appointment::getAppointmentId)
        .containsExactly(2L);
    assertThat(withoutFirst.queue()).containsExactly(queued(TEN, 45));
    assertThat(withoutFirst.bookedCount()).isEqualTo(2);
    assertThat(withoutFirst.countOverlapping(new Slot(NINE, NINE.plusSeconds(1800)))).isZero();
  }

  @Test
  void withoutAnUnknownAppointmentChangesNothing() {
    BookedDay day = new BookedDay(List.of(appointment(1L, NINE)), List.of(queued(NINE, 30)), 1);

    assertThat(day.without(99L)).isEqualTo(day);
  }

  private static Appointment appointment(Long appointmentId, Instant start) {
    Appointment appointment = new Appointment(9L, 1L, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    ReflectionTestUtils.setField(appointment, "appointmentId", appointmentId);
    return appointment;
  }

  private static ScheduledService queued(Instant start, int minutes) {
    return new ScheduledService(start, Duration.ofMinutes(minutes));
  }
}
