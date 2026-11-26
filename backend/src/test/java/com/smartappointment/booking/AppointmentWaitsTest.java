package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.STAFF_ID;
import static com.smartappointment.booking.SlotFixtures.THURSDAY;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.estimation.ScheduledService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** SRS Eq. 4 for an appointment that is already booked. */
@ExtendWith(MockitoExtension.class)
class AppointmentWaitsTest {

  @Mock private BookedDayLoader bookedDayLoader;

  @Test
  void aLateRunBeforeTheAppointmentIsItsWait() {
    Appointment first = appointment(1L, localTime(MONDAY, 9, 0));
    Appointment second = appointment(2L, localTime(MONDAY, 9, 30));
    when(bookedDayLoader.load(STAFF_ID, MONDAY, ISTANBUL))
        .thenReturn(
            new BookedDay(
                List.of(first, second), List.of(queued(first, 45), queued(second, 30)), 2));

    assertThat(waits().estimatedWaitMinutes(second, advising(1, NO_RULE))).isEqualTo(15);
    assertThat(waits().estimatedWaitMinutes(first, advising(1, NO_RULE))).isZero();
  }

  @Test
  void anAppointmentInServiceOrFinishedHasNoWait() {
    Appointment inService = appointment(1L, localTime(MONDAY, 9, 0));
    inService.setStatus(AppointmentStatus.IN_SERVICE);

    assertThat(waits().estimatedWaitMinutes(inService, advising(1, NO_RULE))).isNull();
    verifyNoInteractions(bookedDayLoader);
  }

  private AppointmentWaits waits() {
    return new AppointmentWaits(bookedDayLoader, Clock.fixed(localTime(THURSDAY, 10, 0), ISTANBUL));
  }

  private static Appointment appointment(Long appointmentId, Instant start) {
    Appointment appointment = new Appointment(9L, STAFF_ID, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    ReflectionTestUtils.setField(appointment, "appointmentId", appointmentId);
    return appointment;
  }

  private static ScheduledService queued(Appointment appointment, int learnedMinutes) {
    return new ScheduledService(appointment.getStartTime(), Duration.ofMinutes(learnedMinutes));
  }
}
