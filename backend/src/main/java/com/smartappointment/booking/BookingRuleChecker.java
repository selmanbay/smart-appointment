package com.smartappointment.booking;

import com.smartappointment.catalog.StaffLookupService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The booking rules of SRS 2.5 in the order of SDD Activity 1: lead time and advance limit, the
 * customer's active-booking limit, working hours, then the staff row lock and the capacity count.
 * The caller has already checked that the staff member provides the service, calls the steps in
 * order, and holds the transaction the lock belongs to. Reschedule (UC-4) runs the same steps
 * without the active-booking limit, since moving an appointment does not add one.
 */
@Component
class BookingRuleChecker {

  private final AppointmentRepository appointmentRepository;
  private final BookedDayLoader bookedDayLoader;
  private final StaffLookupService staffLookupService;
  private final Clock clock;

  BookingRuleChecker(
      AppointmentRepository appointmentRepository,
      BookedDayLoader bookedDayLoader,
      StaffLookupService staffLookupService,
      Clock clock) {
    this.appointmentRepository = appointmentRepository;
    this.bookedDayLoader = bookedDayLoader;
    this.staffLookupService = staffLookupService;
    this.clock = clock;
  }

  /**
   * The rules of a new booking that need no lock: lead time, advance limit, active-booking limit,
   * working hours. Returns the requested slot of the day's grid.
   */
  Slot checkRequest(BookingContext context, Long customerId, Instant start) {
    checkWindow(context, start);
    BookingProblem.ACTIVE_LIMIT_REACHED.unless(hasRoomForAnotherBooking(context, customerId));
    return requireSlot(context, start);
  }

  /** The rules of a move (UC-4) that need no lock: lead time, advance limit, working hours. */
  Slot checkMove(BookingContext context, Instant start) {
    checkWindow(context, start);
    return requireSlot(context, start);
  }

  /** UC-4, UC-5: a customer changes only a BOOKED appointment, and only before the deadline. */
  void checkCustomerMayChange(Appointment appointment, BookingContext context) {
    BookingProblem.INVALID_STATUS.unless(appointment.getStatus() == AppointmentStatus.BOOKED);
    BookingProblem.CANCEL_DEADLINE_PASSED.unless(
        CancellationRule.canCustomerCancel(
            appointment, context.rule().cancelDeadlineHours(), clock.instant()));
  }

  /** SDD Activity 1: lock the staff row until the transaction ends (ADR-0018). */
  void lockStaffMember(BookingContext context) {
    staffLookupService.lockStaffMember(context.staffId());
  }

  /**
   * Under the lock: the capacity count (SLOT_TAKEN). Returns the slot's estimated wait in minutes
   * (Eq. 4).
   */
  long checkCapacity(BookingContext context, Slot slot) {
    return checkCapacity(context, slot, loadDay(context, slot));
  }

  /** As {@link #checkCapacity}, with the moved appointment left out of the count (UC-4). */
  long checkCapacityForMove(BookingContext context, Slot slot, Long movedAppointmentId) {
    return checkCapacity(context, slot, loadDay(context, slot).without(movedAppointmentId));
  }

  private long checkCapacity(BookingContext context, Slot slot, BookedDay bookedDay) {
    BookingProblem.SLOT_TAKEN.unless(bookedDay.countOverlapping(slot) < context.capacity());
    Instant firstPossibleStart =
        SlotWaitEstimator.firstPossibleStart(dayGridOf(context, slot.start()), clock.instant());
    return bookedDay
        .waitEstimator(context.capacity(), firstPossibleStart)
        .estimateWait(slot, context.learnedDuration())
        .toMinutes();
  }

  private void checkWindow(BookingContext context, Instant start) {
    BookingWindow window = BookingWindow.forRule(context.rule(), context.zone(), clock.instant());
    BookingProblem.LEAD_TIME_NOT_MET.unless(window.isLeadTimeMet(start));
    BookingProblem.TOO_FAR_IN_ADVANCE.unless(window.isWithinAdvanceLimit(start));
  }

  private static Slot requireSlot(BookingContext context, Instant start) {
    return dayGridOf(context, start).stream()
        .filter(candidate -> candidate.start().equals(start))
        .findFirst()
        .orElseThrow(BookingProblem.OUTSIDE_WORKING_HOURS::exception);
  }

  private BookedDay loadDay(BookingContext context, Slot slot) {
    LocalDate date = LocalDate.ofInstant(slot.start(), context.zone());
    return bookedDayLoader.load(context.staffId(), date, context.zone());
  }

  private static List<Slot> dayGridOf(BookingContext context, Instant start) {
    return SlotGenerator.generate(context, LocalDate.ofInstant(start, context.zone()));
  }

  /** SRS 2.5 max_active_per_user, counted per service: the rule belongs to the service. */
  private boolean hasRoomForAnotherBooking(BookingContext context, Long customerId) {
    Integer maxActivePerUser = context.rule().maxActivePerUser();
    if (maxActivePerUser == null) {
      return true;
    }
    long active =
        appointmentRepository.countByCustomerIdAndServiceIdAndStatusIn(
            customerId, context.service().serviceId(), AppointmentStatus.HOLDING_CAPACITY);
    return active < maxActivePerUser;
  }
}
