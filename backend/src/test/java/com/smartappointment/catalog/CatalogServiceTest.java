package com.smartappointment.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.OrganizationService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

  @Mock private ServiceRepository serviceRepository;
  @Mock private OrganizationService organizationService;

  @Test
  void listActiveServicesReturnsOnlyWhatTheRepositoryMarksActive() {
    when(organizationService.existsById(1L)).thenReturn(true);
    Service advising = new Service(1L, "Academic advising", 30);
    when(serviceRepository.findByOrganizationIdAndIsActiveTrue(1L)).thenReturn(List.of(advising));

    List<ServiceResponse> services =
        new CatalogService(serviceRepository, organizationService).listActiveServices(1L);

    assertThat(services)
        .singleElement()
        .satisfies(s -> assertThat(s.name()).isEqualTo("Academic advising"));
  }

  @Test
  void listActiveServicesThrowsWhenTheOrganizationIsMissing() {
    when(organizationService.existsById(404L)).thenReturn(false);

    assertThatThrownBy(() ->
            new CatalogService(serviceRepository, organizationService).listActiveServices(404L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void recordServiceDurationAppliesEq3ToTheLockedService() {
    Service advising = new Service(1L, "Academic advising", 30);
    advising.setLearnedDurationMin(new BigDecimal("30.00"));
    when(serviceRepository.findLockedById(1L)).thenReturn(Optional.of(advising));

    BigDecimal learned =
        new CatalogService(serviceRepository, organizationService)
            .recordServiceDuration(1L, Duration.ofMinutes(40));

    // SRS Eq. 3: 0.2 * 40 + 0.8 * 30 = 32.
    assertThat(learned).isEqualByComparingTo("32.00");
    assertThat(advising.getLearnedDurationMin()).isEqualByComparingTo("32.00");
  }

  @Test
  void theFirstCompletionStartsFromTheExpectedDuration() {
    Service documents = new Service(1L, "Document request", 15);
    when(serviceRepository.findLockedById(2L)).thenReturn(Optional.of(documents));

    BigDecimal learned =
        new CatalogService(serviceRepository, organizationService)
            .recordServiceDuration(2L, Duration.ofMinutes(20));

    // ADR-0017: 0.2 * 20 + 0.8 * 15 = 16.
    assertThat(learned).isEqualByComparingTo("16.00");
  }
}
