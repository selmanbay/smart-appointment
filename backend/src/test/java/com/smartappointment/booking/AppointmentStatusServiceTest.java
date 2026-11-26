package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.STAFF_ID;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.catalog.CatalogService;
import com.smartappointment.common.web.BusinessRuleException;
import com.smartappointment.common.web.ForbiddenException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** UC-8: who records a status, and what each change stores. */
@ExtendWith(MockitoExtension.class)
class AppointmentStatusServiceTest {

  private static final long APPOINTMENT_ID = 42L;
  private static final Instant NOW = localTime(MONDAY, 9, 40);
  private static final AccountResponse MAYA =
      new AccountResponse(4L, "Maya Chen", "m@example.com", null, UserRole.STAFF, 1L, STAFF_ID);
  private static final AccountResponse DANIEL =
      new AccountResponse(5L, "Daniel Brooks", "d@example.com", null, UserRole.STAFF, 1L, 2L);
  private static final AccountResponse OWNER =
      new AccountResponse(
          2L, "Hannah Reid", "h@example.com", null, UserRole.ORGANIZATION_OWNER, 1L, null);
  private static final AccountResponse CUSTOMER =
      new AccountResponse(9L, "Alex Morgan", "a@example.com", null, UserRole.CUSTOMER, null, null);

  @Mock private AppointmentChangeLoader appointmentChangeLoader;
  @Mock private CatalogService catalogService;
  @Mock private AppointmentResponseMapper appointmentResponseMapper;

  @Test
  void theAppointmentsStaffMemberChecksTheCustomerIn() {
    Appointment appointment = booked();
    givenCaller(appointment, MAYA);

    service().changeStatus(APPOINTMENT_ID, MAYA.userId(), AppointmentStatus.CHECKED_IN);

    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
    assertThat(appointment.getCheckedInAt()).isEqualTo(NOW);
    verifyNoInteractions(catalogService);
  }

  @Test
  void completionUpdatesTheLearnedDurationWithTheMeasuredTime() {
    Appointment appointment = booked();
    appointment.setStatus(AppointmentStatus.IN_SERVICE);
    appointment.setServiceStartAt(NOW.minus(Duration.ofMinutes(35)));
    givenCaller(appointment, MAYA);

    service().changeStatus(APPOINTMENT_ID, MAYA.userId(), AppointmentStatus.COMPLETED);

    assertThat(appointment.getServiceEndAt()).isEqualTo(NOW);
    verify(catalogService).recordServiceDuration(1L, Duration.ofMinutes(35));
  }

  @Test
  void theOwnerMayRecordAStatus() {
    Appointment appointment = booked();
    givenCaller(appointment, OWNER);

    service().changeStatus(APPOINTMENT_ID, OWNER.userId(), AppointmentStatus.NO_SHOW);

    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
  }

  @Test
  void anotherStaffMemberOrTheCustomerIsForbidden() {
    Appointment appointment = booked();
    givenCaller(appointment, DANIEL);
    assertThatThrownBy(() ->
            service().changeStatus(APPOINTMENT_ID, DANIEL.userId(), AppointmentStatus.CHECKED_IN))
        .isInstanceOf(ForbiddenException.class);

    givenCaller(appointment, CUSTOMER);
    assertThatThrownBy(() ->
            service().changeStatus(APPOINTMENT_ID, CUSTOMER.userId(), AppointmentStatus.CHECKED_IN))
        .isInstanceOf(ForbiddenException.class);
    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
  }

  @Test
  void aSkippedStepIsRejectedAndChangesNothing() {
    Appointment appointment = booked();
    givenCaller(appointment, MAYA);

    assertThatThrownBy(() ->
            service().changeStatus(APPOINTMENT_ID, MAYA.userId(), AppointmentStatus.COMPLETED))
        .isInstanceOfSatisfying(
            BusinessRuleException.class,
            exception -> assertThat(exception.getCode()).isEqualTo("INVALID_STATUS"));
    assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    assertThat(appointment.getServiceEndAt()).isNull();
    verifyNoInteractions(catalogService);
  }

  private void givenCaller(Appointment appointment, AccountResponse caller) {
    when(appointmentChangeLoader.load(APPOINTMENT_ID, caller.userId()))
        .thenReturn(new AppointmentChange(appointment, advising(1, NO_RULE), caller));
  }

  private static Appointment booked() {
    Instant start = localTime(MONDAY, 9, 0);
    Appointment appointment = new Appointment(9L, STAFF_ID, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    return appointment;
  }

  private AppointmentStatusService service() {
    return new AppointmentStatusService(
        appointmentChangeLoader,
        catalogService,
        appointmentResponseMapper,
        Clock.fixed(NOW, ISTANBUL));
  }
}
