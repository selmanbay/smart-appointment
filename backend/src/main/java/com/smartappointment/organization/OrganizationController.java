package com.smartappointment.organization;

import com.smartappointment.organization.dto.OrganizationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation getOrganization. */
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

  private final OrganizationService organizationService;

  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @GetMapping("/{organizationId}")
  public OrganizationResponse getOrganization(@PathVariable Long organizationId) {
    return organizationService.getOrganization(organizationId);
  }
}
