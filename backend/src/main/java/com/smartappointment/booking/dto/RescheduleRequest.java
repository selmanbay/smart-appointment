package com.smartappointment.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/** Body of POST /api/v1/appointments/{appointmentId}/reschedule (contract: RescheduleRequest). */
public record RescheduleRequest(@NotNull Long staffId, @NotNull OffsetDateTime start) {}
