package com.smartappointment.booking.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Body of POST /api/v1/appointments/{appointmentId}/status (contract: StatusChangeRequest). Only
 * the statuses staff record are accepted; any other value is a 400.
 */
public record StatusChangeRequest(@NotNull RecordedStatus status) {

  /** The contract's enum: BOOKED and CANCELLED are not set through this operation. */
  public enum RecordedStatus {
    CHECKED_IN,
    IN_SERVICE,
    COMPLETED,
    NO_SHOW
  }
}
