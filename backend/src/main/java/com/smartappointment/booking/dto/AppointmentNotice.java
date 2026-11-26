package com.smartappointment.booking.dto;

import java.time.OffsetDateTime;

/**
 * What a customer's notice about an appointment says (UC-20); the start in the organization's zone.
 */
public record AppointmentNotice(
    Long appointmentId,
    String customerEmail,
    String customerName,
    String serviceName,
    String staffName,
    OffsetDateTime start) {}
