package com.smartappointment.auth.dto;

import com.smartappointment.auth.UserRole;

/** The signed-in account (contract: Account). */
public record AccountResponse(
    Long userId,
    String fullName,
    String email,
    String phone,
    UserRole role,
    Long organizationId,
    Long staffId) {}
