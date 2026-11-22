package com.smartappointment.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Saves and reads an {@link AppUser} against real PostgreSQL (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AppUserRepositoryTest {

  @Test
  void savesAndReadsAnAppUser(@Autowired AppUserRepository appUserRepository) {
    AppUser customer =
        new AppUser("Alex Morgan", "alex.morgan@example.com", "{bcrypt}hash", UserRole.CUSTOMER);
    customer.setPhone("+90 555 000 0000");

    Long savedId = appUserRepository.save(customer).getUserId();
    AppUser loaded = appUserRepository.findById(savedId).orElseThrow();

    assertThat(loaded.getFullName()).isEqualTo("Alex Morgan");
    assertThat(loaded.getEmail()).isEqualTo("alex.morgan@example.com");
    assertThat(loaded.getRole()).isEqualTo(UserRole.CUSTOMER);
    assertThat(loaded.getOrganizationId()).isNull();
    assertThat(loaded.getCreatedAt()).isNotNull();
    assertThat(appUserRepository.findByEmail("alex.morgan@example.com")).contains(loaded);
  }
}
