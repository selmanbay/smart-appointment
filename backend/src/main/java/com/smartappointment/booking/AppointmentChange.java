package com.smartappointment.booking;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.common.web.ForbiddenException;

/** An existing appointment someone wants to change (cancel, reschedule, status), and who asks. */
record AppointmentChange(Appointment appointment, BookingContext context, AccountResponse caller) {

  boolean isCustomer() {
    return AccessRules.isCustomer(appointment, caller);
  }

  /** 403 unless the caller is the appointment's customer. */
  void requireCustomer(String action) {
    if (!isCustomer()) {
      throw new ForbiddenException("Only the customer can " + action + " this appointment.");
    }
  }

  /** UC-14: the owner of the appointment's organization. */
  boolean isOrganizationOwner() {
    return AccessRules.isOrganizationOwner(caller, context.service().organizationId());
  }

  /** 403 unless the caller serves the appointment or owns its organization. */
  void requireStaffOrOwner() {
    if (!AccessRules.isAppointmentStaff(appointment, caller) && !isOrganizationOwner()) {
      throw new ForbiddenException("Only the appointment's staff member can record its status.");
    }
  }
}
