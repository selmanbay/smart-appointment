package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.STAFF_ID;
import static com.smartappointment.booking.SlotFixtures.THURSDAY;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.bookedAt;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static com.smartappointment.booking.SlotFixtures.nothingBooked;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.BusinessRuleException;
import com.smartappointment.estimation.ScheduledService;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Every booking rule of SRS 2.5, in the order of SDD Activity 1. */
@ExtendWith(MockitoExtension.class)
class BookingRuleCheckerTest {

  private static final long CUSTOMER_ID = 9L;
  private static final Instant THURSDAY_TEN = localTime(THURSDAY, 10, 0);

  @Mock private AppointmentRepository appointmentRepository;
  @Mock private BookedDayLoader bookedDayLoader;
  @Mock private StaffLookupService staffLookupService;

  private BookingRuleChecker checker;

  @BeforeEach
  void setUp() {
    checker =
        new BookingRuleChecker(
            appointmentRepository,
            bookedDayLoader,
            staffLookupService,
            Clock.fixed(THURSDAY_TEN, ISTANBUL));
    lenient()
        .when(bookedDayLoader.load(eq(STAFF_ID), any(), eq(ISTANBUL)))
        .thenReturn(nothingBooked());
  }

  @Test
  void aFreeSlotPassesAndTheCapacityIsCountedUnderTheLock() {
    Instant mondayNine = localTime(MONDAY, 9, 0);

    BookingContext context = advising(1, NO_RULE);
    Slot slot = checker.checkRequest(context, CUSTOMER_ID, mondayNine);
    checker.lockStaffMember(context);
    long estimatedWaitMinutes = checker.checkCapacity(context, slot);

    assertThat(slot.start()).isEqualTo(mondayNine);
    assertThat(slot.end()).isEqualTo(localTime(MONDAY, 9, 30));
    assertThat(estimatedWaitMinutes).isZero();
    InOrder lockThenCount = inOrder(staffLookupService, bookedDayLoader);
    lockThenCount.verify(staffLookupService).lockStaffMember(STAFF_ID);
    lockThenCount.verify(bookedDayLoader).load(STAFF_ID, MONDAY, ISTANBUL);
  }

  @Test
  void aStartTooSoonIsRejectedBeforeAnyLock() {
    AppointmentRuleResponse twoHoursAhead = new AppointmentRuleResponse(null, 2, null, null);

    assertRejected(advising(1, twoHoursAhead), localTime(THURSDAY, 11, 0), "LEAD_TIME_NOT_MET");
    verifyNoInteractions(staffLookupService);
  }

  @Test
  void aStartTooFarAheadIsRejected() {
    AppointmentRuleResponse threeDays = new AppointmentRuleResponse(3, null, null, null);

    assertRejected(advising(1, threeDays), localTime(MONDAY, 9, 0), "TOO_FAR_IN_ADVANCE");
  }

  @Test
  void aCustomerAtTheActiveLimitForTheServiceIsRejected() {
    AppointmentRuleResponse twoActive = new AppointmentRuleResponse(null, null, null, 2);
    lenient()
        .when(
            appointmentRepository.countByCustomerIdAndServiceIdAndStatusIn(
                eq(CUSTOMER_ID), anyLong(), any()))
        .thenReturn(2L);

    assertRejected(advising(1, twoActive), localTime(MONDAY, 9, 0), "ACTIVE_LIMIT_REACHED");
  }

  @Test
  void aStartInTheBreakOrOffTheSlotGridIsOutsideWorkingHours() {
    assertRejected(advising(1, NO_RULE), localTime(MONDAY, 12, 0), "OUTSIDE_WORKING_HOURS");
    assertRejected(advising(1, NO_RULE), localTime(MONDAY, 9, 10), "OUTSIDE_WORKING_HOURS");
    assertRejected(advising(1, NO_RULE), localTime(MONDAY, 16, 0), "OUTSIDE_WORKING_HOURS");
  }

  @Test
  void aFullSlotIsTaken() {
    lenient()
        .when(bookedDayLoader.load(STAFF_ID, MONDAY, ISTANBUL))
        .thenReturn(bookedAt(localTime(MONDAY, 9, 0), 30));

    assertRejected(advising(1, NO_RULE), localTime(MONDAY, 9, 0), "SLOT_TAKEN");
  }

  @Test
  void capacityTwoTakesASecondBookingForTheSameSlot() {
    lenient()
        .when(bookedDayLoader.load(STAFF_ID, MONDAY, ISTANBUL))
        .thenReturn(bookedAt(localTime(MONDAY, 9, 0), 30));

    assertThat(book(advising(2, NO_RULE), localTime(MONDAY, 9, 0))).isZero();
  }

  @Test
  void theEstimatedWaitIncludesALateRunBeforeTheSlot() {
    lenient()
        .when(bookedDayLoader.load(STAFF_ID, MONDAY, ISTANBUL))
        .thenReturn(bookedAt(localTime(MONDAY, 9, 0), 45));

    assertThat(book(advising(1, NO_RULE), localTime(MONDAY, 9, 30))).isEqualTo(15);
  }

  @Test
  void aCustomerMayChangeABookedAppointmentOnlyBeforeTheDeadline() {
    AppointmentRuleResponse twelveHours = new AppointmentRuleResponse(null, null, 12, null);
    Appointment mondayNine = appointment(1L, localTime(MONDAY, 9, 0));
    Appointment thursdayTwenty = appointment(2L, localTime(THURSDAY, 20, 0));

    checker.checkCustomerMayChange(mondayNine, advising(1, twelveHours));
    assertThatThrownBy(
            () -> checker.checkCustomerMayChange(thursdayTwenty, advising(1, twelveHours)))
        .isInstanceOfSatisfying(
            BusinessRuleException.class,
            exception -> assertThat(exception.getCode()).isEqualTo("CANCEL_DEADLINE_PASSED"));
  }

  @Test
  void onlyABookedAppointmentCanBeChanged() {
    Appointment checkedIn = appointment(1L, localTime(MONDAY, 9, 0));
    checkedIn.setStatus(AppointmentStatus.CHECKED_IN);

    assertThatThrownBy(() -> checker.checkCustomerMayChange(checkedIn, advising(1, NO_RULE)))
        .isInstanceOfSatisfying(
            BusinessRuleException.class,
            exception -> assertThat(exception.getCode()).isEqualTo("INVALID_STATUS"));
  }

  @Test
  void aMoveIgnoresTheActiveLimitBecauseItAddsNoAppointment() {
    AppointmentRuleResponse oneActive = new AppointmentRuleResponse(null, null, null, 1);
    lenient()
        .when(appointmentRepository.countByCustomerIdAndServiceIdAndStatusIn(any(), any(), any()))
        .thenReturn(1L);

    Slot slot = checker.checkMove(advising(1, oneActive), localTime(MONDAY, 10, 0));

    assertThat(slot.start()).isEqualTo(localTime(MONDAY, 10, 0));
  }

  @Test
  void aMoveDoesNotCompeteWithTheAppointmentBeingMoved() {
    // Moving 09:00 to 09:30 on a 45-minute run: the old booking overlaps the new slot.
    Appointment moving = appointment(7L, localTime(MONDAY, 9, 0));
    moving.setEndTime(localTime(MONDAY, 9, 45));
    lenient()
        .when(bookedDayLoader.load(STAFF_ID, MONDAY, ISTANBUL))
        .thenReturn(
            new BookedDay(
                List.of(moving),
                List.of(new ScheduledService(localTime(MONDAY, 9, 0), Duration.ofMinutes(45))),
                1));
    BookingContext context = advising(1, NO_RULE);
    Slot slot = checker.checkMove(context, localTime(MONDAY, 9, 30));

    assertThat(checker.checkCapacityForMove(context, slot, 7L)).isZero();
    assertThatThrownBy(() -> checker.checkCapacity(context, slot))
        .isInstanceOf(BusinessRuleException.class);
  }

  private static Appointment appointment(Long appointmentId, Instant start) {
    Appointment appointment = new Appointment(CUSTOMER_ID, STAFF_ID, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    ReflectionTestUtils.setField(appointment, "appointmentId", appointmentId);
    return appointment;
  }

  /** The three steps in the order BookingService runs them; returns the estimated wait. */
  private long book(BookingContext context, Instant start) {
    Slot slot = checker.checkRequest(context, CUSTOMER_ID, start);
    checker.lockStaffMember(context);
    return checker.checkCapacity(context, slot);
  }

  private void assertRejected(BookingContext context, Instant start, String code) {
    assertThatThrownBy(() -> book(context, start))
        .isInstanceOfSatisfying(
            BusinessRuleException.class,
            exception -> assertThat(exception.getCode()).isEqualTo(code));
  }
}
