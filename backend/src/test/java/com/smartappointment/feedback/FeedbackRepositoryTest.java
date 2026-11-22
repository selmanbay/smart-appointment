package com.smartappointment.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.AppUser;
import com.smartappointment.auth.UserRole;
import com.smartappointment.booking.Appointment;
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

/** Saves and reads a {@link Feedback} against real PostgreSQL (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class FeedbackRepositoryTest {

  @Test
  void savesAndReadsFeedback(
      @Autowired TestEntityManager entityManager,
      @Autowired FeedbackRepository feedbackRepository) {
    Appointment appointment = aCompletedAppointment(entityManager);

    Feedback feedback = new Feedback(appointment.getAppointmentId(), (short) 5);
    feedback.setComment("Quick and helpful.");
    Long feedbackId = feedbackRepository.save(feedback).getFeedbackId();

    Feedback loaded = feedbackRepository.findById(feedbackId).orElseThrow();
    assertThat(loaded.getAppointmentId()).isEqualTo(appointment.getAppointmentId());
    assertThat(loaded.getRating()).isEqualTo((short) 5);
    assertThat(loaded.getComment()).isEqualTo("Quick and helpful.");
    assertThat(loaded.getCreatedAt()).isNotNull();
    assertThat(feedbackRepository.findByAppointmentId(appointment.getAppointmentId()))
        .contains(loaded);
  }

  private Appointment aCompletedAppointment(TestEntityManager entityManager) {
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

    Instant startTime = Instant.now().minus(Duration.ofDays(1)).truncatedTo(ChronoUnit.MICROS);
    Appointment appointment =
        new Appointment(
            customer.getUserId(), staff.getStaffId(), service.getServiceId(), startTime);
    appointment.setEndTime(startTime.plus(Duration.ofMinutes(30)));
    entityManager.persist(appointment);
    entityManager.flush();
    return appointment;
  }
}
