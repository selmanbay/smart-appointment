package com.smartappointment.schedule.dto;

/** The contract operation getWorkingHours (contract: WorkingHours). */
public record WorkingHoursResponse(
    short dayOfWeek, String startTime, String endTime, String breakStart, String breakEnd) {}
