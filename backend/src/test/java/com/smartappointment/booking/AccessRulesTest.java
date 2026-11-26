package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.STAFF_ID;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccessRulesTest {

  private static final long ORGANIZATION_ID = 1L;
  private static final AccountResponse CUSTOMER =
      new AccountResponse(9L, "Alex Morgan", "a@example.com", null, UserRole.CUSTOMER, null, null);
  private static final AccountResponse OTHER_CUSTOMER =
      new AccountResponse(8L, "Sofia Martin", "s@example.com", null, UserRole.CUSTOMER, null, null);
  private static final AccountResponse STAFF =
      new AccountResponse(4L, "Maya Chen", "m@example.com", null, UserRole.STAFF, 1L, STAFF_ID);
  private static final AccountResponse OWNER =
      new AccountResponse(
          2L, "Hannah Reid", "h@example.com", null, UserRole.ORGANIZATION_OWNER, 1L, null);
  private static final AccountResponse OTHER_OWNER =
      new AccountResponse(
          3L, "Other Owner", "o@example.com", null, UserRole.ORGANIZATION_OWNER, 7L, null);
  private static final AppointmentRuleResponse TWELVE_HOURS =
      new AppointmentRuleResponse(null, null, 12, null);

  @Test
  void theCustomerTheStaffMemberAndTheOwnerSeeTheAppointment() {
    Appointment appointment = booked();

    assertThat(AccessRules.canView(appointment, CUSTOMER, ORGANIZATION_ID)).isTrue();
    assertThat(AccessRules.canView(appointment, STAFF, ORGANIZATION_ID)).isTrue();
    assertThat(AccessRules.canView(appointment, OWNER, ORGANIZATION_ID)).isTrue();
    assertThat(AccessRules.canView(appointment, OTHER_CUSTOMER, ORGANIZATION_ID)).isFalse();
    assertThat(AccessRules.canView(appointment, OTHER_OWNER, ORGANIZATION_ID)).isFalse();
  }

  @Test
  void theCustomerCanCancelOnlyBeforeTheDeadline() {
    Appointment appointment = booked();
    Instant thirteenHoursBefore = appointment.getStartTime().minus(Duration.ofHours(13));
    Instant elevenHoursBefore = appointment.getStartTime().minus(Duration.ofHours(11));

    assertThat(
            AccessRules.canCancel(
                appointment, advising(1, TWELVE_HOURS), CUSTOMER, thirteenHoursBefore))
        .isTrue();
    assertThat(
            AccessRules.canCancel(
                appointment, advising(1, TWELVE_HOURS), CUSTOMER, elevenHoursBefore))
        .isFalse();
  }

  @Test
  void theOwnerCanCancelAnyBookedAppointmentAndTheStaffMemberNone() {
    Appointment appointment = booked();
    Instant elevenHoursBefore = appointment.getStartTime().minus(Duration.ofHours(11));

    assertThat(
            AccessRules.canCancel(appointment, advising(1, TWELVE_HOURS), OWNER, elevenHoursBefore))
        .isTrue();
    assertThat(
            AccessRules.canCancel(appointment, advising(1, TWELVE_HOURS), STAFF, elevenHoursBefore))
        .isFalse();
    appointment.setStatus(AppointmentStatus.COMPLETED);
    assertThat(
            AccessRules.canCancel(appointment, advising(1, TWELVE_HOURS), OWNER, elevenHoursBefore))
        .isFalse();
  }

  private static Appointment booked() {
    Instant start = localTime(MONDAY, 9, 0);
    Appointment appointment = new Appointment(CUSTOMER.userId(), STAFF_ID, 1L, start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    return appointment;
  }
}
