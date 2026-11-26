package com.smartappointment.booking;

import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.estimation.ScheduledService;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingPeriod;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/** The demo organization's day for slot tests: Europe/Istanbul, 09:00-16:00, lunch 12:00-13:00. */
final class SlotFixtures {

  static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");
  static final LocalDate THURSDAY = LocalDate.of(2026, 11, 26);
  static final LocalDate MONDAY = LocalDate.of(2026, 11, 30);
  static final List<WorkingPeriod> NINE_TO_FOUR_WITH_LUNCH =
      List.of(
          new WorkingPeriod(LocalTime.of(9, 0), LocalTime.of(12, 0)),
          new WorkingPeriod(LocalTime.of(13, 0), LocalTime.of(16, 0)));
  static final AppointmentRuleResponse NO_RULE =
      new AppointmentRuleResponse(null, null, null, null);

  static final long STAFF_ID = 1L;
  private static final long SERVICE_ID = 1L;

  private SlotFixtures() {}

  static BookingContext advising(int capacity, AppointmentRuleResponse rule) {
    return context(30, 0, capacity, rule);
  }

  static BookingContext context(
      int durationMinutes, int bufferMinutes, int capacity, AppointmentRuleResponse rule) {
    ServiceResponse service =
        new ServiceResponse(
            SERVICE_ID, 1L, "Academic advising", null, durationMinutes, null, bufferMinutes, true);
    return new BookingContext(STAFF_ID, service, capacity, ISTANBUL, NINE_TO_FOUR_WITH_LUNCH, rule);
  }

  static Instant localTime(LocalDate date, int hour, int minute) {
    return date.atTime(hour, minute).atZone(ISTANBUL).toInstant();
  }

  /** One booking that takes 30 minutes of the schedule and runs for {@code learnedMinutes}. */
  static BookedDay bookedAt(Instant start, int learnedMinutes) {
    Appointment appointment = new Appointment(9L, 1L, SERVICE_ID, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    return new BookedDay(
        List.of(appointment),
        List.of(new ScheduledService(start, Duration.ofMinutes(learnedMinutes))),
        1);
  }

  static BookedDay nothingBooked() {
    return new BookedDay(List.of(), List.of(), 0);
  }
}
