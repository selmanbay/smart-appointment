package com.smartappointment.catalog.dto;

/** An active staff member who provides a service, as staff recommendation lists them. */
public record StaffMemberSummary(Long staffId, String fullName, String title, int capacity) {}
