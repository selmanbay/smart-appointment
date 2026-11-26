package com.smartappointment.booking;

import java.time.Instant;

/** A time range [start, end) in one staff member's day. */
record Slot(Instant start, Instant end) {

  boolean overlaps(Instant otherStart, Instant otherEnd) {
    return start.isBefore(otherEnd) && otherStart.isBefore(end);
  }
}
