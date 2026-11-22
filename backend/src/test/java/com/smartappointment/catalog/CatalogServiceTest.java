package com.smartappointment.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.OrganizationService;
import java.util.List;
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
}
