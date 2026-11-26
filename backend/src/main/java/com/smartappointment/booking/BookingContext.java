package com.smartappointment.booking;

import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.estimation.LearnedDuration;
import com.smartappointment.estimation.SlotsPerDay;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingPeriod;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

/**
 * What slot generation and booking need about one staff member, one service and one day. A service
 * without an appointment rule carries a rule whose limits are all null (SRS 2.5).
 */
record BookingContext(
    Long staffId,
    ServiceResponse service,
    int capacity,
    ZoneId zone,
    List<WorkingPeriod> workingPeriods,
    AppointmentRuleResponse rule) {

  Duration expectedDuration() {
    return Duration.ofMinutes(service.expectedDurationMinutes());
  }

  Duration buffer() {
    return Duration.ofMinutes(service.bufferMinutes());
  }

  /** The service's duration for Eq. 4 (ADR-0017). */
  Duration learnedDuration() {
    return LearnedDuration.current(
        service.learnedDurationMinutes(), service.expectedDurationMinutes());
  }

  /** SRS Eq. 1: the appointments the staff member can take that day, capacity included. */
  int slotsPerDay() {
    List<Duration> periodLengths =
        workingPeriods.stream()
            .map(period -> Duration.between(period.start(), period.end()))
            .toList();
    return SlotsPerDay.count(periodLengths, expectedDuration(), buffer(), capacity);
  }
}
