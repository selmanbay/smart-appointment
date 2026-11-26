package com.smartappointment.organization.dto;

import java.util.Map;

/** The contract operation getOrganization (contract: Organization). */
public record OrganizationResponse(
    Long organizationId,
    String name,
    String address,
    String timezone,
    String logoUrl,
    Map<String, String> terminology,
    String ownerName,
    boolean isActive) {}
