package com.smartappointment.notification;

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

/** Saves and reads a {@link Notification} against real PostgreSQL (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class NotificationRepositoryTest {

  @Test
  void savesAndReadsANotification(
      @Autowired TestEntityManager entityManager,
      @Autowired NotificationRepository notificationRepository) {
    Appointment appointment = anAppointment(entityManager);
    Instant scheduledFor = appointment.getStartTime().minus(Duration.ofHours(24));

    Notification notification =
        new Notification(
            appointment.getAppointmentId(),
            NotificationChannel.EMAIL,
            NotificationType.CONFIRMATION,
            scheduledFor);
    Long notificationId = notificationRepository.save(notification).getNotificationId();

    Notification loaded = notificationRepository.findById(notificationId).orElseThrow();
    assertThat(loaded.getAppointmentId()).isEqualTo(appointment.getAppointmentId());
    assertThat(loaded.getChannel()).isEqualTo(NotificationChannel.EMAIL);
    assertThat(loaded.getType()).isEqualTo(NotificationType.CONFIRMATION);
    assertThat(loaded.getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(loaded.getScheduledFor()).isEqualTo(scheduledFor);
  }

  private Appointment anAppointment(TestEntityManager entityManager) {
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

    Instant startTime = Instant.now().plus(Duration.ofDays(2)).truncatedTo(ChronoUnit.MICROS);
    Appointment appointment =
        new Appointment(
            customer.getUserId(), staff.getStaffId(), service.getServiceId(), startTime);
    appointment.setEndTime(startTime.plus(Duration.ofMinutes(30)));
    entityManager.persist(appointment);
    entityManager.flush();
    return appointment;
  }
}
