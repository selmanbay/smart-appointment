package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Every pair of statuses against SRS 2.5 (AGENT_RULES section 7: every status transition). */
class StatusTransitionTest {

  private static final Instant NOW = Instant.parse("2026-11-30T06:05:00Z");
  private static final Set<List<AppointmentStatus>> ALLOWED =
      Set.of(
          List.of(AppointmentStatus.BOOKED, AppointmentStatus.CHECKED_IN),
          List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SERVICE),
          List.of(AppointmentStatus.IN_SERVICE, AppointmentStatus.COMPLETED),
          List.of(AppointmentStatus.BOOKED, AppointmentStatus.NO_SHOW));

  static Stream<Arguments> everyPairOfStatuses() {
    List<Arguments> pairs = new ArrayList<>();
    for (AppointmentStatus from : AppointmentStatus.values()) {
      for (AppointmentStatus to : AppointmentStatus.values()) {
        pairs.add(Arguments.of(from, to));
      }
    }
    return pairs.stream();
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("everyPairOfStatuses")
  void onlyTheTransitionsOfSrs25AreAllowed(AppointmentStatus from, AppointmentStatus to) {
    boolean isAllowed = ALLOWED.contains(List.of(from, to));

    assertThat(StatusTransition.find(from, to).isPresent()).isEqualTo(isAllowed);
  }

  @Test
  void eachStepStampsItsOwnTimeAndSetsTheStatus() {
    Appointment appointment = booked();

    StatusTransition.CHECK_IN.apply(appointment, NOW);
    assertThat(appointment.getCheckedInAt()).isEqualTo(NOW);
    StatusTransition.START_SERVICE.apply(appointment, NOW.plusSeconds(60));
    assertThat(appointment.getServiceStartAt()).isEqualTo(NOW.plusSeconds(60));
    StatusTransition.COMPLETE.apply(appointment, NOW.plusSeconds(60 * 31));

    assertThat(appointment.getServiceEndAt()).isEqualTo(NOW.plusSeconds(60 * 31));
    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    assertThat(appointment.measuredServiceDuration()).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  void aNoShowRecordsNoServiceTimes() {
    Appointment appointment = booked();

    StatusTransition.MARK_NO_SHOW.apply(appointment, NOW);

    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    assertThat(appointment.getCheckedInAt()).isNull();
    assertThat(appointment.getServiceStartAt()).isNull();
    assertThat(StatusTransition.MARK_NO_SHOW.completesService()).isFalse();
  }

  private static Appointment booked() {
    Instant start = Instant.parse("2026-11-30T06:00:00Z");
    Appointment appointment = new Appointment(9L, 1L, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    return appointment;
  }
}
