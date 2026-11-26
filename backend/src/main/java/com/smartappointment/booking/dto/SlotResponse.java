package com.smartappointment.booking.dto;

import java.time.OffsetDateTime;

/** One free slot (contract: Slot), with the offset of the organization's time zone. */
public record SlotResponse(
    OffsetDateTime start, OffsetDateTime end, int remainingCapacity, long estimatedWaitMinutes) {}
