package com.smartappointment.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.smartappointment.common.web.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock private OrganizationRepository organizationRepository;

  @Test
  void getOrganizationMapsEveryField() {
    Organization organization = new Organization("Northbridge University", "Europe/Istanbul");
    organization.setAddress("Main Campus");
    organization.setTerminology(java.util.Map.of("customer", "student"));
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

    var response = new OrganizationService(organizationRepository).getOrganization(1L);

    assertThat(response.name()).isEqualTo("Northbridge University");
    assertThat(response.address()).isEqualTo("Main Campus");
    assertThat(response.timezone()).isEqualTo("Europe/Istanbul");
    assertThat(response.terminology()).containsEntry("customer", "student");
    assertThat(response.isActive()).isTrue();
  }

  @Test
  void getOrganizationThrowsWhenMissing() {
    when(organizationRepository.findById(404L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new OrganizationService(organizationRepository).getOrganization(404L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void existsByIdDelegatesToTheRepository() {
    when(organizationRepository.existsById(1L)).thenReturn(true);

    assertThat(new OrganizationService(organizationRepository).existsById(1L)).isTrue();
  }
}
