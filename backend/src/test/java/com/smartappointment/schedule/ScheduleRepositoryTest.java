package com.smartappointment.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.AppUser;
import com.smartappointment.auth.UserRole;
import com.smartappointment.catalog.Service;
import com.smartappointment.catalog.ServiceRepository;
import com.smartappointment.catalog.Staff;
import com.smartappointment.organization.Organization;
import com.smartappointment.organization.OrganizationRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Saves and reads {@link WorkingHours}, {@link AppointmentRule} and {@link ScheduleChangeRequest}
 * against real PostgreSQL (ddl-auto: validate).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class ScheduleRepositoryTest {

  @Test
  void savesAndReadsWorkingHoursAndAScheduleChangeRequest(
      @Autowired TestEntityManager entityManager,
      @Autowired WorkingHoursRepository workingHoursRepository,
      @Autowired ScheduleChangeRequestRepository scheduleChangeRequestRepository) {
    Staff staff = staffForNewUser(entityManager, "Daniel Brooks");

    WorkingHours workingHours =
        new WorkingHours(staff.getStaffId(), (short) 1, LocalTime.of(9, 0), LocalTime.of(16, 0));
    workingHours.setBreakStart(LocalTime.of(12, 0));
    workingHours.setBreakEnd(LocalTime.of(13, 0));
    Long workingHoursId = workingHoursRepository.save(workingHours).getWorkingHoursId();

    WorkingHours loadedHours = workingHoursRepository.findById(workingHoursId).orElseThrow();
    assertThat(loadedHours.getStaffId()).isEqualTo(staff.getStaffId());
    assertThat(loadedHours.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(loadedHours.getBreakEnd()).isEqualTo(LocalTime.of(13, 0));

    ScheduleChangeRequest request =
        new ScheduleChangeRequest(
            staff.getStaffId(), (short) 1, LocalTime.of(10, 0), LocalTime.of(17, 0));
    Long requestId = scheduleChangeRequestRepository.save(request).getRequestId();

    ScheduleChangeRequest loadedRequest =
        scheduleChangeRequestRepository.findById(requestId).orElseThrow();
    assertThat(loadedRequest.getStatus()).isEqualTo(ScheduleChangeStatus.PENDING);
    assertThat(loadedRequest.getRequestedAt()).isNotNull();
  }

  @Test
  void savesAndReadsAnAppointmentRule(
      @Autowired OrganizationRepository organizationRepository,
      @Autowired ServiceRepository serviceRepository,
      @Autowired AppointmentRuleRepository appointmentRuleRepository) {
    Organization organization =
        organizationRepository.save(new Organization("Northbridge University", "Europe/Istanbul"));
    Service service =
        serviceRepository.save(
            new Service(organization.getOrganizationId(), "Document request", 15));

    AppointmentRule rule = new AppointmentRule(service.getServiceId());
    rule.setMinLeadHours(2);
    rule.setMaxAdvanceDays(30);
    rule.setCancelDeadlineHours(24);
    rule.setMaxActivePerUser(3);
    Long ruleId = appointmentRuleRepository.save(rule).getRuleId();

    AppointmentRule loaded = appointmentRuleRepository.findById(ruleId).orElseThrow();
    assertThat(loaded.getMinLeadHours()).isEqualTo(2);
    assertThat(loaded.getMaxAdvanceDays()).isEqualTo(30);
    assertThat(appointmentRuleRepository.findByServiceId(service.getServiceId())).contains(loaded);
  }

  private Staff staffForNewUser(TestEntityManager entityManager, String fullName) {
    Organization organization = new Organization("Northbridge University", "Europe/Istanbul");
    entityManager.persist(organization);
    AppUser user =
        new AppUser(
            fullName,
            fullName.toLowerCase().replace(" ", ".") + "@example.com",
            "{bcrypt}hash",
            UserRole.STAFF);
    user.setOrganizationId(organization.getOrganizationId());
    entityManager.persist(user);
    Staff staff = new Staff(user.getUserId(), 1);
    entityManager.persist(staff);
    entityManager.flush();
    return staff;
  }
}
