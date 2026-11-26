package com.smartappointment.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.AppUser;
import com.smartappointment.auth.AppUserRepository;
import com.smartappointment.auth.UserRole;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.organization.Organization;
import com.smartappointment.organization.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Saves and reads {@link Staff} and {@link Service} against real PostgreSQL (ddl-auto: validate).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class CatalogRepositoryTest {

  @Test
  void savesAndReadsStaffAndTheServicesTheyProvide(
      @Autowired OrganizationRepository organizationRepository,
      @Autowired AppUserRepository appUserRepository,
      @Autowired StaffRepository staffRepository,
      @Autowired ServiceRepository serviceRepository) {
    Organization organization =
        organizationRepository.save(new Organization("Northbridge University", "Europe/Istanbul"));
    AppUser staffUser =
        new AppUser("Maya Chen", "maya.chen@example.com", "{bcrypt}hash", UserRole.STAFF);
    staffUser.setOrganizationId(organization.getOrganizationId());
    staffUser = appUserRepository.save(staffUser);
    Service service =
        serviceRepository.save(
            new Service(organization.getOrganizationId(), "Academic advising", 30));

    Staff staff = new Staff(staffUser.getUserId(), 1);
    staff.setTitle("Academic Advisor");
    staff.getServices().add(service);
    Long savedId = staffRepository.save(staff).getStaffId();

    Staff loaded = staffRepository.findById(savedId).orElseThrow();

    assertThat(loaded.getUserId()).isEqualTo(staffUser.getUserId());
    assertThat(loaded.getTitle()).isEqualTo("Academic Advisor");
    assertThat(loaded.getCapacity()).isEqualTo(1);
    assertThat(loaded.isActive()).isTrue();
    assertThat(loaded.getServices())
        .extracting(Service::getServiceId)
        .containsExactly(service.getServiceId());
    assertThat(staffRepository.findByUserId(staffUser.getUserId())).contains(loaded);
  }

  @Test
  void findsAnActiveStaffMemberOnlyForTheServicesTheyProvide(
      @Autowired OrganizationRepository organizationRepository,
      @Autowired AppUserRepository appUserRepository,
      @Autowired StaffRepository staffRepository,
      @Autowired ServiceRepository serviceRepository) {
    Organization organization =
        organizationRepository.save(new Organization("Northbridge University", "Europe/Istanbul"));
    AppUser staffUser =
        new AppUser("Daniel Brooks", "daniel.brooks@example.com", "{bcrypt}hash", UserRole.STAFF);
    staffUser.setOrganizationId(organization.getOrganizationId());
    staffUser = appUserRepository.save(staffUser);
    Service advising =
        serviceRepository.save(
            new Service(organization.getOrganizationId(), "Academic advising", 30));
    Service documents =
        serviceRepository.save(
            new Service(organization.getOrganizationId(), "Document request", 15));
    Staff staff = new Staff(staffUser.getUserId(), 2);
    staff.getServices().add(advising);
    Long staffId = staffRepository.save(staff).getStaffId();

    assertThat(
            staffRepository.findByStaffIdAndIsActiveTrueAndServicesServiceId(
                staffId, advising.getServiceId()))
        .map(Staff::getCapacity)
        .contains(2);
    assertThat(
            staffRepository.findByStaffIdAndIsActiveTrueAndServicesServiceId(
                staffId, documents.getServiceId()))
        .isEmpty();
    staff.setTitle("Senior advisor");
    staffRepository.saveAndFlush(staff);
    assertThat(staffRepository.findActiveProviders(advising.getServiceId()))
        .containsExactly(new StaffMemberSummary(staffId, "Daniel Brooks", "Senior advisor", 2));
    assertThat(staffRepository.findActiveProviders(documents.getServiceId())).isEmpty();

    staff.setActive(false);
    staffRepository.saveAndFlush(staff);
    assertThat(
            staffRepository.findByStaffIdAndIsActiveTrueAndServicesServiceId(
                staffId, advising.getServiceId()))
        .isEmpty();
    assertThat(staffRepository.findActiveProviders(advising.getServiceId())).isEmpty();
  }
}
