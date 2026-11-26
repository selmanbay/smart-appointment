package com.smartappointment.booking;

import com.smartappointment.estimation.ScheduledService;
import com.smartappointment.estimation.WaitingTimeEstimate;
import com.smartappointment.estimation.WaitingTimeEstimator;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Estimated wait of a new appointment placed in a slot (SRS Eq. 4): the day's bookings that start
 * no later than the slot run first, then the new appointment. A booking at the same start time is
 * served first, so the estimate is never optimistic.
 */
final class SlotWaitEstimator {

  private final List<ScheduledService> bookedInStartOrder;
  private final int capacity;
  private final Instant firstPossibleStart;

  /**
   * {@code firstPossibleStart} is {@code F_0}: the current time today, the opening time on a later
   * day.
   */
  SlotWaitEstimator(
      List<ScheduledService> bookedInStartOrder, int capacity, Instant firstPossibleStart) {
    this.bookedInStartOrder = bookedInStartOrder;
    this.capacity = capacity;
    this.firstPossibleStart = firstPossibleStart;
  }

  /** SRS Eq. 4: F_0 is the current time today and the opening time on a later day. */
  static Instant firstPossibleStart(List<Slot> dayGrid, Instant now) {
    Instant openingTime = dayGrid.getFirst().start();
    return now.isAfter(openingTime) ? now : openingTime;
  }

  Duration estimateWait(Slot slot, Duration learnedDuration) {
    List<ScheduledService> queue = new ArrayList<>();
    for (ScheduledService booked : bookedInStartOrder) {
      if (!booked.scheduledStart().isAfter(slot.start())) {
        queue.add(booked);
      }
    }
    queue.add(new ScheduledService(slot.start(), learnedDuration));
    List<WaitingTimeEstimate> estimates =
        WaitingTimeEstimator.estimate(queue, capacity, firstPossibleStart);
    return estimates.getLast().waitingTime();
  }
}
