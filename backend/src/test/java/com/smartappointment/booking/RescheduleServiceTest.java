package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.RescheduleRequest;
import com.smartappointment.common.web.BusinessRuleException;
import com.smartappointment.common.web.ForbiddenException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** UC-4: the old slot is released only when the new one is taken. */
@ExtendWith(MockitoExtension.class)
class RescheduleServiceTest {

  private static final long APPOINTMENT_ID = 42L;
  private static final long CUSTOMER_ID = 9L;
  private static final long NEW_STAFF_ID = 2L;
  private static final Instant OLD_START = localTime(MONDAY, 9, 0);
  private static final Instant NEW_START = localTime(MONDAY, 14, 0);
  private static final RescheduleRequest REQUEST =
      new RescheduleRequest(NEW_STAFF_ID, NEW_START.atZone(ISTANBUL).toOffsetDateTime());
  private static final AccountResponse CUSTOMER =
      new AccountResponse(
          CUSTOMER_ID, "Alex Morgan", "a@example.com", null, UserRole.CUSTOMER, null, null);

  @Mock private AppointmentChangeLoader appointmentChangeLoader;
  @Mock private BookingRecorder bookingRecorder;
  @Mock private BookingRuleChecker bookingRuleChecker;
  @Mock private AppointmentResponseMapper appointmentResponseMapper;

  private Appointment appointment;
  private BookingContext originalContext;
  private BookingContext target;
  private Slot newSlot;

  @BeforeEach
  void setUp() {
    appointment = new Appointment(CUSTOMER_ID, 1L, 1L, OLD_START);
    appointment.setEndTime(OLD_START.plus(Duration.ofMinutes(30)));
    ReflectionTestUtils.setField(appointment, "appointmentId", APPOINTMENT_ID);
    originalContext = advising(1, NO_RULE);
    target =
        new BookingContext(
            NEW_STAFF_ID,
            originalContext.service(),
            1,
            ISTANBUL,
            originalContext.workingPeriods(),
            NO_RULE);
    newSlot = new Slot(NEW_START, NEW_START.plus(Duration.ofMinutes(30)));
  }

  @Test
  void theAppointmentMovesToTheNewStaffMemberAndSlot() {
    givenCaller(CUSTOMER);
    givenTarget();
    when(bookingRuleChecker.checkCapacityForMove(target, newSlot, APPOINTMENT_ID)).thenReturn(5L);

    service().reschedule(APPOINTMENT_ID, CUSTOMER_ID, REQUEST);

    verify(bookingRuleChecker).lockStaffMember(target);
    verify(bookingRecorder).recordMove(appointment, target, newSlot);
  }

  @Test
  void aTakenNewSlotKeepsTheOriginalAppointment() {
    givenCaller(CUSTOMER);
    givenTarget();
    when(bookingRuleChecker.checkCapacityForMove(target, newSlot, APPOINTMENT_ID))
        .thenThrow(BookingProblem.SLOT_TAKEN.exception());

    assertThatThrownBy(() -> service().reschedule(APPOINTMENT_ID, CUSTOMER_ID, REQUEST))
        .isInstanceOf(BusinessRuleException.class);
    verifyNoInteractions(bookingRecorder);
    assertThat(appointment.getStartTime()).isEqualTo(OLD_START);
  }

  @Test
  void pastTheDeadlineNothingIsLockedOrMoved() {
    givenCaller(CUSTOMER);
    doThrow(BookingProblem.CANCEL_DEADLINE_PASSED.exception())
        .when(bookingRuleChecker)
        .checkCustomerMayChange(appointment, originalContext);

    assertThatThrownBy(() -> service().reschedule(APPOINTMENT_ID, CUSTOMER_ID, REQUEST))
        .isInstanceOfSatisfying(
            BusinessRuleException.class,
            exception -> assertThat(exception.getCode()).isEqualTo("CANCEL_DEADLINE_PASSED"));
    verify(appointmentChangeLoader, never()).loadMoveTarget(any(), any(), any());
    verifyNoInteractions(bookingRecorder);
  }

  @Test
  void onlyTheCustomerMayReschedule() {
    AccountResponse owner =
        new AccountResponse(
            2L, "Hannah Reid", "h@example.com", null, UserRole.ORGANIZATION_OWNER, 1L, null);
    givenCaller(owner);

    assertThatThrownBy(() -> service().reschedule(APPOINTMENT_ID, owner.userId(), REQUEST))
        .isInstanceOf(ForbiddenException.class);
    verifyNoInteractions(bookingRuleChecker, bookingRecorder);
  }

  private void givenCaller(AccountResponse caller) {
    when(appointmentChangeLoader.load(APPOINTMENT_ID, caller.userId()))
        .thenReturn(new AppointmentChange(appointment, originalContext, caller));
  }

  private void givenTarget() {
    when(appointmentChangeLoader.loadMoveTarget(appointment, NEW_STAFF_ID, NEW_START))
        .thenReturn(target);
    when(bookingRuleChecker.checkMove(target, NEW_START)).thenReturn(newSlot);
  }

  private RescheduleService service() {
    return new RescheduleService(
        appointmentChangeLoader, bookingRuleChecker, bookingRecorder, appointmentResponseMapper);
  }
}
