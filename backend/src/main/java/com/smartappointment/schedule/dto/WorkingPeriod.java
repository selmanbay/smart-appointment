package com.smartappointment.schedule.dto;

import java.time.LocalTime;

/** One continuous working period of a day, in the organization's time zone (SRS Eq. 1, L_j). */
public record WorkingPeriod(LocalTime start, LocalTime end) {}
