package com.smartappointment.booking;

import com.smartappointment.booking.dto.SlotResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contract operation listFreeSlots (UC-3 step 4, UC-4, UC-19), and the day staff ranking uses. */
@Service
public class SlotService {

  private final BookingContextLoader bookingContextLoader;
  private final BookedDayLoader bookedDayLoader;
  private final Clock clock;

  SlotService(
      BookingContextLoader bookingContextLoader, BookedDayLoader bookedDayLoader, Clock clock) {
    this.bookingContextLoader = bookingContextLoader;
    this.bookedDayLoader = bookedDayLoader;
    this.clock = clock;
  }

  /**
   * The day's slot grid (Eq. 1) limited by the appointment rule and the booked capacity, each slot
   * with its estimated wait (Eq. 4). Throws 404 for a missing or inactive service, or a staff
   * member who does not provide it.
   */
  @Transactional(readOnly = true)
  public List<SlotResponse> freeSlots(Long staffId, Long serviceId, LocalDate date) {
    return staffDay(staffId, serviceId, date).freeSlots();
  }

  /** The free slots plus the day's workload (SRS Eq. 1); same 404s as {@link #freeSlots}. */
  @Transactional(readOnly = true)
  StaffDay staffDay(Long staffId, Long serviceId, LocalDate date) {
    BookingContext context = bookingContextLoader.load(staffId, serviceId, date);
    List<Slot> dayGrid = SlotGenerator.generate(context, date);
    if (dayGrid.isEmpty()) {
      return StaffDay.DAY_OFF;
    }
    BookedDay bookedDay = bookedDayLoader.load(staffId, date, context.zone());
    return new StaffDay(
        FreeSlots.select(dayGrid, context, bookedDay, clock.instant()),
        context.slotsPerDay(),
        bookedDay.bookedCount());
  }
}
