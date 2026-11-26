package com.smartappointment.booking;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * The status changes staff record (SRS 2.5, UC-8, SDD Activity 2), as a table rather than an
 * if-chain (AGENT_RULES section 13): {@code BOOKED -> CHECKED_IN -> IN_SERVICE -> COMPLETED} and
 * {@code BOOKED -> NO_SHOW}. Each entry stamps its time from the server clock, so recorded
 * durations cannot be edited by clients. Cancellation is not here: it has its own rules (UC-5).
 */
enum StatusTransition {
  CHECK_IN(AppointmentStatus.BOOKED, AppointmentStatus.CHECKED_IN, Appointment::setCheckedInAt),
  START_SERVICE(
      AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SERVICE, Appointment::setServiceStartAt),
  COMPLETE(AppointmentStatus.IN_SERVICE, AppointmentStatus.COMPLETED, Appointment::setServiceEndAt),
  MARK_NO_SHOW(AppointmentStatus.BOOKED, AppointmentStatus.NO_SHOW, (appointment, time) -> {});

  private final AppointmentStatus from;
  private final AppointmentStatus to;
  private final BiConsumer<Appointment, Instant> stamp;

  StatusTransition(
      AppointmentStatus from, AppointmentStatus to, BiConsumer<Appointment, Instant> stamp) {
    this.from = from;
    this.to = to;
    this.stamp = stamp;
  }

  /** Empty when SRS 2.5 does not allow the change. */
  static Optional<StatusTransition> find(AppointmentStatus from, AppointmentStatus to) {
    return Arrays.stream(values())
        .filter(transition -> transition.from == from && transition.to == to)
        .findFirst();
  }

  void apply(Appointment appointment, Instant now) {
    stamp.accept(appointment, now);
    appointment.setStatus(to);
  }

  boolean completesService() {
    return to == AppointmentStatus.COMPLETED;
  }
}
