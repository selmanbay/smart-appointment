package com.smartappointment.booking;

import com.smartappointment.estimation.WaitingTimeEstimate;
import com.smartappointment.estimation.WaitingTimeEstimator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The estimated wait of a booked appointment (SRS Eq. 4) over its staff member's day as it is now:
 * the appointments that still hold capacity run in start order from F_0 (now today, the opening
 * time on a later day). Computed on request, never stored (AGENT_RULES section 8).
 */
@Component
class AppointmentWaits {

  private final BookedDayLoader bookedDayLoader;
  private final Clock clock;

  AppointmentWaits(BookedDayLoader bookedDayLoader, Clock clock) {
    this.bookedDayLoader = bookedDayLoader;
    this.clock = clock;
  }

  /** Null when the appointment is not waiting (in service, finished or cancelled). */
  Long estimatedWaitMinutes(Appointment appointment, BookingContext context) {
    if (!appointment.getStatus().isWaitingForService()) {
      return null;
    }
    LocalDate date = LocalDate.ofInstant(appointment.getStartTime(), context.zone());
    BookedDay day = bookedDayLoader.load(context.staffId(), date, context.zone());
    List<WaitingTimeEstimate> estimates =
        WaitingTimeEstimator.estimate(
            day.queue(), context.capacity(), firstPossibleStart(context, date));
    for (int i = 0; i < day.holdingCapacity().size(); i++) {
      if (day.holdingCapacity().get(i).getAppointmentId().equals(appointment.getAppointmentId())) {
        return estimates.get(i).waitingTime().toMinutes();
      }
    }
    return null;
  }

  private Instant firstPossibleStart(BookingContext context, LocalDate date) {
    List<Slot> dayGrid = SlotGenerator.generate(context, date);
    Instant now = clock.instant();
    return dayGrid.isEmpty() ? now : SlotWaitEstimator.firstPossibleStart(dayGrid, now);
  }
}
