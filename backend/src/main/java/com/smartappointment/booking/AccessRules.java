package com.smartappointment.booking;

import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import java.time.Instant;
import java.util.Objects;

/**
 * Who may see and change an appointment: its customer, the staff member who serves it, and the
 * owner of its organization (UC-6, UC-8, UC-14). Used by every booking operation, so the rules are
 * written once.
 */
final class AccessRules {

  private AccessRules() {}

  static boolean isCustomer(Appointment appointment, AccountResponse account) {
    return appointment.getCustomerId().equals(account.userId());
  }

  /** UC-8 (SDD Sequence 3): the staff member who serves the appointment. */
  static boolean isAppointmentStaff(Appointment appointment, AccountResponse account) {
    return account.staffId() != null && account.staffId().equals(appointment.getStaffId());
  }

  /** UC-14: the owner of the given organization. */
  static boolean isOrganizationOwner(AccountResponse account, Long organizationId) {
    return account.role() == UserRole.ORGANIZATION_OWNER
        && Objects.equals(account.organizationId(), organizationId);
  }

  /** The contract's getAppointment: its customer, its staff member or the owner. */
  static boolean canView(Appointment appointment, AccountResponse account, Long organizationId) {
    return isCustomer(appointment, account)
        || isAppointmentStaff(appointment, account)
        || isOrganizationOwner(account, organizationId);
  }

  /**
   * The contract's {@code canCancel}: the owner for any BOOKED appointment, the customer until the
   * cancellation deadline (UC-5, UC-14); nobody else.
   */
  static boolean canCancel(
      Appointment appointment, BookingContext context, AccountResponse account, Instant now) {
    if (isOrganizationOwner(account, context.service().organizationId())) {
      return appointment.getStatus() == AppointmentStatus.BOOKED;
    }
    return isCustomer(appointment, account)
        && CancellationRule.canCustomerCancel(
            appointment, context.rule().cancelDeadlineHours(), now);
  }
}
