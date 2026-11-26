package com.smartappointment.booking;

import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * The time limits of an appointment rule for a new booking (SRS 2.5): the start must be at least
 * {@code min_lead_hours} from now and at most {@code max_advance_days} calendar days ahead, counted
 * in the organization's time zone. A missing limit does not apply, but a start in the past never
 * qualifies.
 */
record BookingWindow(Instant earliestStart, LocalDate lastDay, ZoneId zone) {

  static BookingWindow forRule(AppointmentRuleResponse rule, ZoneId zone, Instant now) {
    Instant earliestStart =
        rule.minLeadHours() == null ? now : now.plus(Duration.ofHours(rule.minLeadHours()));
    LocalDate lastDay =
        rule.maxAdvanceDays() == null
            ? LocalDate.MAX
            : LocalDate.ofInstant(now, zone).plusDays(rule.maxAdvanceDays());
    return new BookingWindow(earliestStart, lastDay, zone);
  }

  boolean isLeadTimeMet(Instant start) {
    return !start.isBefore(earliestStart);
  }

  boolean isWithinAdvanceLimit(Instant start) {
    return !LocalDate.ofInstant(start, zone).isAfter(lastDay);
  }

  boolean allows(Instant start) {
    return isLeadTimeMet(start) && isWithinAdvanceLimit(start);
  }
}
