package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.THURSDAY;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

/** Every change a customer makes is written and announced for the notice e-mail (UC-20). */
@ExtendWith(MockitoExtension.class)
class BookingRecorderTest {

  private static final Instant NOW = localTime(THURSDAY, 10, 0);
  private static final Instant NINE = localTime(MONDAY, 9, 0);

  @Mock private AppointmentRepository appointmentRepository;
  @Mock private BookingIdempotencyKeyRepository idempotencyKeyRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @Test
  void aNewBookingIsAnnouncedAsBooked() {
    when(appointmentRepository.saveAndFlush(any()))
        .thenAnswer(invocation -> withId(invocation.getArgument(0), 42L));

    recorder().record(9L, advising(1, NO_RULE), new Slot(NINE, NINE.plusSeconds(1800)), null);

    verify(eventPublisher).publishEvent(new AppointmentEvent(42L, AppointmentEvent.Change.BOOKED));
  }

  @Test
  void aMoveIsAnnouncedAsRescheduled() {
    Appointment appointment = withId(booked(), 42L);
    Instant two = localTime(MONDAY, 14, 0);

    recorder().recordMove(appointment, advising(1, NO_RULE), new Slot(two, two.plusSeconds(1800)));

    assertThat(appointment.getStartTime()).isEqualTo(two);
    verify(eventPublisher)
        .publishEvent(new AppointmentEvent(42L, AppointmentEvent.Change.RESCHEDULED));
  }

  @Test
  void aCancellationKeepsTheRowStampsTheTimeAndIsAnnounced() {
    Appointment appointment = withId(booked(), 42L);

    recorder().recordCancellation(appointment);

    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    assertThat(appointment.getCancelledAt()).isEqualTo(NOW);
    verify(eventPublisher)
        .publishEvent(new AppointmentEvent(42L, AppointmentEvent.Change.CANCELLED));
  }

  private BookingRecorder recorder() {
    return new BookingRecorder(
        appointmentRepository,
        idempotencyKeyRepository,
        eventPublisher,
        Clock.fixed(NOW, ISTANBUL));
  }

  private static Appointment booked() {
    Appointment appointment = new Appointment(9L, 1L, 1L, NINE);
    appointment.setEndTime(NINE.plus(Duration.ofMinutes(30)));
    return appointment;
  }

  private static Appointment withId(Appointment appointment, Long appointmentId) {
    ReflectionTestUtils.setField(appointment, "appointmentId", appointmentId);
    return appointment;
  }
}
