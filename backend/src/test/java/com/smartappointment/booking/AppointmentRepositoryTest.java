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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Saves and reads an {@link Appointment} against real PostgreSQL (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AppointmentRepositoryTest {

  @Test
  void findOverlappingReturnsOnlyCapacityHoldingAppointmentsInTheRangeInStartOrder(
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
    Instant nine = Instant.parse("2026-11-30T06:00:00Z");
    Appointment later = booked(customer, staff, service, nine.plus(Duration.ofHours(2)));
    Appointment first = booked(customer, staff, service, nine);
    Appointment cancelled = booked(customer, staff, service, nine.plus(Duration.ofHours(1)));
    cancelled.setStatus(AppointmentStatus.CANCELLED);
    Appointment nextDay = booked(customer, staff, service, nine.plus(Duration.ofDays(1)));
    appointmentRepository.saveAll(List.of(later, first, cancelled, nextDay));

    List<Appointment> found =
        appointmentRepository.findOverlapping(
            staff.getStaffId(),
            AppointmentStatus.HOLDING_CAPACITY,
            nine.minus(Duration.ofHours(6)),
            nine.plus(Duration.ofHours(18)));

    assertThat(found)
        .extracting(Appointment::getAppointmentId)
        .containsExactly(first.getAppointmentId(), later.getAppointmentId());
  }

  @Test
  void upcomingIsActiveAndNotEndedNearestFirstAndPastIsTheRestNewestFirst(
      @Autowired TestEntityManager entityManager,
      @Autowired AppointmentRepository appointmentRepository) {
    Organization organization = new Organization("Northbridge University", "Europe/Istanbul");
    entityManager.persist(organization);
    Service service = new Service(organization.getOrganizationId(), "Academic advising", 30);
    entityManager.persist(service);
    AppUser staffUser =
        new AppUser("Daniel Brooks", "daniel.brooks@example.com", "{bcrypt}hash", UserRole.STAFF);
    staffUser.setOrganizationId(organization.getOrganizationId());
    entityManager.persist(staffUser);
    Staff staff = new Staff(staffUser.getUserId(), 1);
    entityManager.persist(staff);
    AppUser customer =
        new AppUser("Sofia Martin", "sofia.martin@example.com", "{bcrypt}hash", UserRole.CUSTOMER);
    entityManager.persist(customer);
    Instant now = Instant.parse("2026-11-26T07:00:00Z");
    Appointment tomorrow = booked(customer, staff, service, now.plus(Duration.ofDays(1)));
    Appointment nextWeek = booked(customer, staff, service, now.plus(Duration.ofDays(7)));
    Appointment cancelledTomorrow = booked(customer, staff, service, now.plus(Duration.ofDays(1)));
    cancelledTomorrow.setStatus(AppointmentStatus.CANCELLED);
    Appointment lastWeek = booked(customer, staff, service, now.minus(Duration.ofDays(7)));
    lastWeek.setStatus(AppointmentStatus.COMPLETED);
    Appointment yesterday = booked(customer, staff, service, now.minus(Duration.ofDays(1)));
    appointmentRepository.saveAll(
        List.of(nextWeek, lastWeek, tomorrow, cancelledTomorrow, yesterday));

    assertThat(
            appointmentRepository
                .findUpcoming(
                    customer.getUserId(),
                    AppointmentStatus.HOLDING_CAPACITY,
                    now,
                    PageRequest.of(0, 20))
                .getContent())
        .extracting(Appointment::getAppointmentId)
        .containsExactly(tomorrow.getAppointmentId(), nextWeek.getAppointmentId());
    Page<Appointment> past =
        appointmentRepository.findPast(
            customer.getUserId(), AppointmentStatus.HOLDING_CAPACITY, now, PageRequest.of(0, 2));
    assertThat(past.getContent())
        .extracting(Appointment::getAppointmentId)
        .containsExactly(cancelledTomorrow.getAppointmentId(), yesterday.getAppointmentId());
    assertThat(past.getTotalElements()).isEqualTo(3);
  }

  private static Appointment booked(AppUser customer, Staff staff, Service service, Instant start) {
    Appointment appointment =
        new Appointment(customer.getUserId(), staff.getStaffId(), service.getServiceId(), start);
    appointment.setEndTime(start.plus(Duration.ofMinutes(30)));
    return appointment;
  }

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

    Long appointmentId = appointmentRepository.saveAndFlush(appointment).getAppointmentId();
    entityManager.clear();
    Object storedEpochSeconds =
        entityManager
            .getEntityManager()
            .createNativeQuery(
                "SELECT floor(extract(epoch FROM start_time))::bigint FROM appointment"
                    + " WHERE appt_id = ?1")
            .setParameter(1, appointmentId)
            .getSingleResult();
    assertThat(((Number) storedEpochSeconds).longValue()).isEqualTo(startTime.getEpochSecond());
    Appointment loaded = appointmentRepository.findById(appointmentId).orElseThrow();

    assertThat(loaded.getCustomerId()).isEqualTo(customer.getUserId());
    assertThat(loaded.getStaffId()).isEqualTo(staff.getStaffId());
    assertThat(loaded.getServiceId()).isEqualTo(service.getServiceId());
    assertThat(loaded.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    assertThat(loaded.getBookedAt()).isNotNull();
    assertThat(loaded.getStartTime()).isEqualTo(startTime);
  }
}
