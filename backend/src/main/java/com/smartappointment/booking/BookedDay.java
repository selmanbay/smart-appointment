package com.smartappointment.booking;

import com.smartappointment.estimation.ScheduledService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A staff member's day: the appointments that still hold capacity, in start order, each with the
 * duration Eq. 4 uses for its service ({@code queue} is in the same order); and how many
 * appointments the day has, cancelled ones not counted (the workload "x of y slots booked").
 */
record BookedDay(List<Appointment> holdingCapacity, List<ScheduledService> queue, int bookedCount) {

  int countOverlapping(Slot slot) {
    return (int)
        holdingCapacity.stream()
            .filter(
                appointment -> slot.overlaps(appointment.getStartTime(), appointment.getEndTime()))
            .count();
  }

  SlotWaitEstimator waitEstimator(int capacity, Instant firstPossibleStart) {
    return new SlotWaitEstimator(queue, capacity, firstPossibleStart);
  }

  /** The day as if the appointment were not booked: a reschedule does not compete with itself. */
  BookedDay without(Long appointmentId) {
    List<Appointment> keptAppointments = new ArrayList<>();
    List<ScheduledService> keptQueue = new ArrayList<>();
    for (int i = 0; i < holdingCapacity.size(); i++) {
      if (!appointmentId.equals(holdingCapacity.get(i).getAppointmentId())) {
        keptAppointments.add(holdingCapacity.get(i));
        keptQueue.add(queue.get(i));
      }
    }
    int removed = holdingCapacity.size() - keptAppointments.size();
    return new BookedDay(keptAppointments, keptQueue, bookedCount - removed);
  }
}
