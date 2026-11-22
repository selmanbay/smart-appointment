package com.smartappointment.organization;

import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.dto.OrganizationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contract operation getOrganization and the organization-existence check other features use. */
@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

  public OrganizationService(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @Transactional(readOnly = true)
  public OrganizationResponse getOrganization(Long organizationId) {
    Organization organization =
        organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
    return toResponse(organization);
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long organizationId) {
    return organizationRepository.existsById(organizationId);
  }

  private OrganizationResponse toResponse(Organization organization) {
    return new OrganizationResponse(
        organization.getOrganizationId(),
        organization.getName(),
        organization.getAddress(),
        organization.getTimezone(),
        organization.getLogo(),
        organization.getTerminology(),
        null,
        organization.isActive());
  }
}
