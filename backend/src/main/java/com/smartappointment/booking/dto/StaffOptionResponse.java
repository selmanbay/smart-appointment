package com.smartappointment.booking.dto;

import java.time.OffsetDateTime;

/**
 * One ranked staff member for a service and day (contract: StaffOption). {@code nextFreeSlot} is
 * null and {@code estimatedWaitMinutes} is 0 when the staff member has no free slot that day.
 */
public record StaffOptionResponse(
    Long staffId,
    String fullName,
    String title,
    int rank,
    boolean isRecommended,
    String recommendationReason,
    OffsetDateTime nextFreeSlot,
    long estimatedWaitMinutes,
    int bookedSlots,
    int totalSlots,
    Double meanRating) {}
