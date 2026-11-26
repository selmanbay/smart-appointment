package com.smartappointment.booking.dto;

import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * A staff member's day (contract: Agenda): its working hours, or null on a day off, and the
 * appointments in time order.
 */
public record AgendaResponse(
    LocalDate date,
    Long staffId,
    WorkingHoursResponse workingHours,
    List<AppointmentResponse> appointments) {}
