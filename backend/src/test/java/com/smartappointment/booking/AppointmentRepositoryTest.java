package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.AppUser;
import com.smartappointment.auth.UserRole;
import com.smartappointment.catalog.Service;
import com.smartappointment.catalog.Staff;
import com.smartappointment.organization.Organization;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Saves and reads an {@link Appointment} against real PostgreSQL (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AppointmentRepositoryTest {

  @Test
  void savesAndReadsAnAppointment(
      @Autowired TestEntityManager entityManager,
      @Autowired AppointmentRepository appointmentRepository) {
    Organization organization = new Organization("Northbridge University", "Europe/Istanbul");
    entityManager.persist(organization);
    Service service = new Service(organization.getOrganizationId(), "Academic advising", 30);
    entityManager.persist(service);
    AppUser staffUser =
        new AppUser("Maya Chen", "maya.chen@example.com", "{bcrypt}hash", UserRole.STAFF);
    staffUser.setOrganizationId(organization.getOrganizationId());
    entityManager.persist(staffUser);
    Staff staff = new Staff(staffUser.getUserId(), 1);
    entityManager.persist(staff);
    AppUser customer =
        new AppUser("Alex Morgan", "alex.morgan@example.com", "{bcrypt}hash", UserRole.CUSTOMER);
    entityManager.persist(customer);
    entityManager.flush();

    Instant startTime = Instant.now().plus(Duration.ofDays(1)).truncatedTo(ChronoUnit.MICROS);
    Appointment appointment =
        new Appointment(
            customer.getUserId(), staff.getStaffId(), service.getServiceId(), startTime);
    appointment.setEndTime(startTime.plus(Duration.ofMinutes(30)));

    Long appointmentId = appointmentRepository.save(appointment).getAppointmentId();
    Appointment loaded = appointmentRepository.findById(appointmentId).orElseThrow();

    assertThat(loaded.getCustomerId()).isEqualTo(customer.getUserId());
    assertThat(loaded.getStaffId()).isEqualTo(staff.getStaffId());
    assertThat(loaded.getServiceId()).isEqualTo(service.getServiceId());
    assertThat(loaded.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    assertThat(loaded.getBookedAt()).isNotNull();
    assertThat(loaded.getStartTime()).isEqualTo(startTime);
  }
}
