package com.smartappointment.catalog;

import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.OrganizationService;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation listServices. The class is annotated with the fully-qualified {@code
 * org.springframework.stereotype.Service} (ADR-0016) because an import of it would shadow the
 * {@link Service} entity this class reads.
 */
@org.springframework.stereotype.Service
public class CatalogService {

  private final ServiceRepository serviceRepository;
  private final OrganizationService organizationService;

  public CatalogService(
      ServiceRepository serviceRepository, OrganizationService organizationService) {
    this.serviceRepository = serviceRepository;
    this.organizationService = organizationService;
  }

  @Transactional(readOnly = true)
  public List<ServiceResponse> listActiveServices(Long organizationId) {
    if (!organizationService.existsById(organizationId)) {
      throw new ResourceNotFoundException("Organization", organizationId);
    }
    return serviceRepository.findByOrganizationIdAndIsActiveTrue(organizationId).stream()
        .map(CatalogService::toResponse)
        .toList();
  }

  private static ServiceResponse toResponse(Service service) {
    return new ServiceResponse(
        service.getServiceId(),
        service.getOrganizationId(),
        service.getName(),
        service.getDescription(),
        service.getExpectedDurationMin(),
        service.getLearnedDurationMin(),
        service.getBufferMin(),
        service.isActive());
  }
}
