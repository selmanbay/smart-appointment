package com.smartappointment.schedule.dto;

/** The contract operation getAppointmentRule (contract: AppointmentRule). */
public record AppointmentRuleResponse(
    Integer maxAdvanceDays,
    Integer minLeadHours,
    Integer cancelDeadlineHours,
    Integer maxActivePerUser) {}
