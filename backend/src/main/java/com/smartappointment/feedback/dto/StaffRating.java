package com.smartappointment.feedback.dto;

/** The mean customer rating of one staff member's appointments (SRS F11). */
public record StaffRating(Long staffId, Double meanRating) {}
