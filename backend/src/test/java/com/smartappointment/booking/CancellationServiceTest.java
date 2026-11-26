package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.common.web.BusinessRuleException;
import com.smartappointment.common.web.ForbiddenException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** UC-5 and UC-14: who may cancel, and what a cancellation changes. */
@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {

  private static final long APPOINTMENT_ID = 42L;
  private static final long CUSTOMER_ID = 9L;
  private static final AccountResponse CUSTOMER =
      new AccountResponse(
          CUSTOMER_ID, "Alex Morgan", "a@example.com", null, UserRole.CUSTOMER, null, null);
  private static final AccountResponse OWNER =
      new AccountResponse(
          2L, "Hannah Reid", "h@example.com", null, UserRole.ORGANIZATION_OWNER, 1L, null);
  private static final AccountResponse OTHER_OWNER =
      new AccountResponse(
          3L, "Other Owner", "o@example.com", null, UserRole.ORGANIZATION_OWNER, 7L, null);

  @Mock private AppointmentChangeLoader appointmentChangeLoader;
  @Mock private BookingRuleChecker bookingRuleChecker;
  @Mock private BookingRecorder bookingRecorder;
  @Mock private AppointmentResponseMapper appointmentResponseMapper;

  @Test
  void theCustomerCancelsBeforeTheDeadline() {
    Appointment appointment = booked();
    BookingContext context = advising(1, NO_RULE);
    givenChange(appointment, context, CUSTOMER);

    service().cancel(APPOINTMENT_ID, CUSTOMER_ID);

    verify(bookingRuleChecker).checkCustomerMayChange(appointment, context);
    verify(bookingRecorder).recordCancellation(appointment);
  }

  @Test
  void aCustomerPastTheDeadlineKeepsTheAppointment() {
    Appointment appointment = booked();
    BookingContext context = advising(1, NO_RULE);
    givenChange(appointment, context, CUSTOMER);
    doThrow(BookingProblem.CANCEL_DEADLINE_PASSED.exception())
        .when(bookingRuleChecker)
        .checkCustomerMayChange(appointment, context);

    assertThatThrownBy(() -> service().cancel(APPOINTMENT_ID, CUSTOMER_ID))
        .isInstanceOf(BusinessRuleException.class);
    verify(bookingRecorder, never()).recordCancellation(appointment);
  }

  @Test
  void theOwnerCancelsAnyBookedAppointmentWithoutTheDeadline() {
    Appointment appointment = booked();
    givenChange(appointment, advising(1, NO_RULE), OWNER);

    service().cancel(APPOINTMENT_ID, OWNER.userId());

    verifyNoInteractions(bookingRuleChecker);
    verify(bookingRecorder).recordCancellation(appointment);
  }

  @Test
  void theOwnerCannotCancelAnAppointmentThatIsNoLongerBooked() {
    Appointment appointment = booked();
    appointment.setStatus(AppointmentStatus.COMPLETED);
    givenChange(appointment, advising(1, NO_RULE), OWNER);

    assertThatThrownBy(() -> service().cancel(APPOINTMENT_ID, OWNER.userId()))
        .isInstanceOfSatisfying(
            BusinessRuleException.class,
            exception -> assertThat(exception.getCode()).isEqualTo("INVALID_STATUS"));
    verifyNoInteractions(bookingRecorder);
  }

  @Test
  void anOwnerOfAnotherOrganizationIsForbidden() {
    Appointment appointment = booked();
    givenChange(appointment, advising(1, NO_RULE), OTHER_OWNER);

    assertThatThrownBy(() -> service().cancel(APPOINTMENT_ID, OTHER_OWNER.userId()))
        .isInstanceOf(ForbiddenException.class);
    verifyNoInteractions(bookingRecorder);
  }

  private void givenChange(
      Appointment appointment, BookingContext context, AccountResponse caller) {
    when(appointmentChangeLoader.load(APPOINTMENT_ID, caller.userId()))
        .thenReturn(new AppointmentChange(appointment, context, caller));
  }

  private static Appointment booked() {
    Instant start = localTime(MONDAY, 9, 0);
    Appointment appointment = new Appointment(CUSTOMER_ID, 1L, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    return appointment;
  }

  private CancellationService service() {
    return new CancellationService(
        appointmentChangeLoader, bookingRuleChecker, bookingRecorder, appointmentResponseMapper);
  }
}
