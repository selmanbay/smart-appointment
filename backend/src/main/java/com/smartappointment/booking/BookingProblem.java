package com.smartappointment.booking;

import com.smartappointment.common.web.BusinessRuleException;

/**
 * The booking rules of SRS 2.5 that can reject a request, with the contract's {@code
 * BookingProblem.code} as the constant name. Each answers 409 and the client returns the customer
 * to slot selection.
 */
enum BookingProblem {
  STAFF_DOES_NOT_PROVIDE_SERVICE(
      "Staff member does not offer this service",
      "This staff member does not offer the service. Choose another staff member."),
  LEAD_TIME_NOT_MET(
      "Too soon to book", "This time is too soon to book for this service. Choose a later time."),
  TOO_FAR_IN_ADVANCE(
      "Too far ahead to book",
      "This service cannot be booked that far ahead. Choose an earlier day."),
  ACTIVE_LIMIT_REACHED(
      "Booking limit reached",
      "You already have the most upcoming appointments allowed for this service."),
  OUTSIDE_WORKING_HOURS(
      "Not a bookable time",
      "This time is not one of the staff member's slots. Choose a slot from the list."),
  SLOT_TAKEN("Slot no longer available", "Someone else booked this slot. Choose another time."),
  CANCEL_DEADLINE_PASSED(
      "Too late to change", "This appointment is too close to its start to cancel or reschedule."),
  INVALID_STATUS(
      "Appointment cannot be changed", "Only a booked appointment can be changed this way.");

  private final String title;
  private final String detail;

  BookingProblem(String title, String detail) {
    this.title = title;
    this.detail = detail;
  }

  BusinessRuleException exception() {
    return new BusinessRuleException(name(), title, detail);
  }

  /** Rejects the request with this problem unless the rule holds. */
  void unless(boolean isRuleMet) {
    if (!isRuleMet) {
      throw exception();
    }
  }
}
