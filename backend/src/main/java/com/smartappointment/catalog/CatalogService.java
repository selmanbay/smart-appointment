package com.smartappointment.catalog;

import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.estimation.LearnedDuration;
import com.smartappointment.organization.OrganizationService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation listServices, and the service lookups booking needs. The class is annotated
 * with the fully-qualified {@code org.springframework.stereotype.Service} (ADR-0016) because an
 * import of it would shadow the {@link Service} entity this class reads.
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

  /** A bookable service: it exists and is active. */
  @Transactional(readOnly = true)
  public ServiceResponse getActiveService(Long serviceId) {
    return serviceRepository
        .findById(serviceId)
        .filter(Service::isActive)
        .map(CatalogService::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
  }

  /** A service active or not, for appointments that are already booked. */
  @Transactional(readOnly = true)
  public ServiceResponse getService(Long serviceId) {
    return serviceRepository
        .findById(serviceId)
        .map(CatalogService::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
  }

  /** The given services, active or not: durations of appointments that are already booked. */
  @Transactional(readOnly = true)
  public List<ServiceResponse> findServices(Collection<Long> serviceIds) {
    return serviceRepository.findAllById(serviceIds).stream()
        .map(CatalogService::toResponse)
        .toList();
  }

  /**
   * SRS Eq. 3 after a completed appointment (UC-8): the service's learned duration moves towards
   * the measured {@code actualDuration}. Locks the service row and joins the caller's transaction,
   * so the update commits or rolls back with the completion. Returns the new learned duration.
   */
  @Transactional
  public BigDecimal recordServiceDuration(Long serviceId, Duration actualDuration) {
    Service service =
        serviceRepository
            .findLockedById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
    BigDecimal learnedDuration =
        LearnedDuration.next(
            service.getLearnedDurationMin(), service.getExpectedDurationMin(), actualDuration);
    service.setLearnedDurationMin(learnedDuration);
    return learnedDuration;
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
