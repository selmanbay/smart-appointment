package com.smartappointment.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/** Body of POST /api/v1/appointments (contract: BookingRequest). */
public record BookingRequest(
    @NotNull Long serviceId, @NotNull Long staffId, @NotNull OffsetDateTime start) {}
