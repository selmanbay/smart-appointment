package com.smartappointment.booking;

import com.smartappointment.schedule.dto.WorkingPeriod;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Lays out one day's slot grid (SRS Eq. 1): each working period is filled from its start with slots
 * of the expected duration, each followed by the buffer. A slot counts only if it and its buffer
 * end inside the period, the same {@code floor(L_j / (d + b))} as Eq. 1, so the grid holds {@code
 * SlotsPerDay.count(...) / capacity} slots.
 */
final class SlotGenerator {

  private SlotGenerator() {}

  static List<Slot> generate(BookingContext context, LocalDate date) {
    Duration duration = context.expectedDuration();
    Duration step = duration.plus(context.buffer());
    List<Slot> slots = new ArrayList<>();
    for (WorkingPeriod period : context.workingPeriods()) {
      Instant start = toInstant(date, period.start(), context.zone());
      Instant periodEnd = toInstant(date, period.end(), context.zone());
      while (!start.plus(step).isAfter(periodEnd)) {
        slots.add(new Slot(start, start.plus(duration)));
        start = start.plus(step);
      }
    }
    return slots;
  }

  private static Instant toInstant(LocalDate date, LocalTime time, ZoneId zone) {
    return date.atTime(time).atZone(zone).toInstant();
  }
}
