package com.smartappointment.catalog.dto;

import java.math.BigDecimal;

/** The contract operation listServices (contract: Service). */
public record ServiceResponse(
    Long serviceId,
    Long organizationId,
    String name,
    String description,
    int expectedDurationMinutes,
    BigDecimal learnedDurationMinutes,
    int bufferMinutes,
    boolean isActive) {}
