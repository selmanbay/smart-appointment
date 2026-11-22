package com.smartappointment.organization;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestcontainersConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Saves and reads an {@link Organization} against real PostgreSQL (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class OrganizationRepositoryTest {

  @Test
  void savesAndReadsAnOrganization(@Autowired OrganizationRepository organizationRepository) {
    Organization organization = new Organization("Northbridge University", "Europe/Istanbul");
    organization.setAddress("1 Campus Way");
    organization.setTerminology(Map.of("appointment", "appointment"));

    Long savedId = organizationRepository.save(organization).getOrganizationId();
    Organization loaded = organizationRepository.findById(savedId).orElseThrow();

    assertThat(loaded.getName()).isEqualTo("Northbridge University");
    assertThat(loaded.getTimezone()).isEqualTo("Europe/Istanbul");
    assertThat(loaded.getAddress()).isEqualTo("1 Campus Way");
    assertThat(loaded.getTerminology()).containsEntry("appointment", "appointment");
    assertThat(loaded.isActive()).isTrue();
  }
}
