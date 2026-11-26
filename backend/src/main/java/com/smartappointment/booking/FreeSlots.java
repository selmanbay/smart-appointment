package com.smartappointment.booking;

import com.smartappointment.booking.dto.SlotResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Picks the free slots out of a day's grid (UC-3 step 4): the appointment rule's lead time and
 * advance limit, then the remaining capacity, then the estimated wait (SRS Eq. 4) of each slot.
 */
final class FreeSlots {

  private FreeSlots() {}

  static List<SlotResponse> select(
      List<Slot> dayGrid, BookingContext context, BookedDay bookedDay, Instant now) {
    if (dayGrid.isEmpty()) {
      return List.of();
    }
    BookingWindow window = BookingWindow.forRule(context.rule(), context.zone(), now);
    SlotWaitEstimator waitEstimator =
        bookedDay.waitEstimator(
            context.capacity(), SlotWaitEstimator.firstPossibleStart(dayGrid, now));
    Duration learnedDuration = context.learnedDuration();
    List<SlotResponse> freeSlots = new ArrayList<>();
    for (Slot slot : dayGrid) {
      int remainingCapacity = context.capacity() - bookedDay.countOverlapping(slot);
      if (window.allows(slot.start()) && remainingCapacity > 0) {
        Duration wait = waitEstimator.estimateWait(slot, learnedDuration);
        freeSlots.add(
            new SlotResponse(
                slot.start().atZone(context.zone()).toOffsetDateTime(),
                slot.end().atZone(context.zone()).toOffsetDateTime(),
                remainingCapacity,
                wait.toMinutes()));
      }
    }
    return freeSlots;
  }
}
