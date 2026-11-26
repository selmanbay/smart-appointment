package com.smartappointment.booking;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Writes the changes a customer makes to appointments (book, move, cancel) and announces each one
 * as an {@link AppointmentEvent}; also finds the result of an earlier request with the same
 * Idempotency-Key.
 */
@Component
class BookingRecorder {

  private final AppointmentRepository appointmentRepository;
  private final BookingIdempotencyKeyRepository idempotencyKeyRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  BookingRecorder(
      AppointmentRepository appointmentRepository,
      BookingIdempotencyKeyRepository idempotencyKeyRepository,
      ApplicationEventPublisher eventPublisher,
      Clock clock) {
    this.appointmentRepository = appointmentRepository;
    this.idempotencyKeyRepository = idempotencyKeyRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /** Empty without a key, or when this customer has not used the key yet. */
  Optional<Appointment> findEarlierResult(Long customerId, UUID idempotencyKey) {
    if (idempotencyKey == null) {
      return Optional.empty();
    }
    return idempotencyKeyRepository
        .findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey)
        .flatMap(key -> appointmentRepository.findById(key.getAppointmentId()));
  }

  /** A BOOKED appointment for the slot; it takes the service's expected duration. */
  Appointment record(Long customerId, BookingContext context, Slot slot, UUID idempotencyKey) {
    Appointment appointment =
        new Appointment(customerId, context.staffId(), context.service().serviceId(), slot.start());
    appointment.setEndTime(slot.end());
    Appointment saved = appointmentRepository.saveAndFlush(appointment);
    if (idempotencyKey != null) {
      idempotencyKeyRepository.save(
          new BookingIdempotencyKey(customerId, idempotencyKey, saved.getAppointmentId()));
    }
    announce(saved, AppointmentEvent.Change.BOOKED);
    return saved;
  }

  /** UC-4: the appointment row moves to the target staff member and slot. */
  void recordMove(Appointment appointment, BookingContext target, Slot slot) {
    appointment.moveTo(target.staffId(), slot.start(), slot.end());
    announce(appointment, AppointmentEvent.Change.RESCHEDULED);
  }

  /** UC-5: marked CANCELLED with the server time, never deleted. */
  void recordCancellation(Appointment appointment) {
    appointment.setStatus(AppointmentStatus.CANCELLED);
    appointment.setCancelledAt(clock.instant());
    announce(appointment, AppointmentEvent.Change.CANCELLED);
  }

  private void announce(Appointment appointment, AppointmentEvent.Change change) {
    eventPublisher.publishEvent(new AppointmentEvent(appointment.getAppointmentId(), change));
  }
}
