package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.jayway.jsonpath.JsonPath;
import com.smartappointment.TestTokens;
import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/** UC-6 and UC-8 reads end to end against PostgreSQL and the demo data. */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AppointmentReadsIT {

  private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void alexSeesUpcomingNearestFirstWithAWaitAndPastNewestFirst() throws Exception {
    String alex = tokenFor("alex.morgan@example.com", UserRole.CUSTOMER);

    String upcoming = read(alex, "/api/v1/me/appointments").getContentAsString();
    String past = read(alex, "/api/v1/me/appointments?scope=PAST&size=5").getContentAsString();

    List<String> upcomingStarts = JsonPath.read(upcoming, "$.content[*].start");
    List<Object> waits = JsonPath.read(upcoming, "$.content[*].estimatedWaitMinutes");
    assertThat(upcomingStarts).isNotEmpty().isSortedAccordingTo(byTime());
    assertThat(waits).doesNotContainNull();
    List<String> pastStarts = JsonPath.read(past, "$.content[*].start");
    assertThat(pastStarts).hasSize(5).isSortedAccordingTo(byTime().reversed());
    assertThat((Integer) JsonPath.read(past, "$.page.totalElements")).isGreaterThan(5);
    assertThat((List<Object>) JsonPath.read(past, "$.content[*].estimatedWaitMinutes"))
        .containsOnlyNulls();
  }

  @Test
  void anAppointmentIsVisibleToItsCustomerStaffMemberAndOwnerOnly() throws Exception {
    long appointmentId =
        jdbcTemplate.queryForObject(
            "SELECT a.appt_id FROM appointment a JOIN app_user u ON u.user_id = a.customer_id"
                + " WHERE u.email = 'alex.morgan@example.com' AND a.status = 'BOOKED'"
                + " ORDER BY a.start_time LIMIT 1",
            Long.class);
    String staffEmail =
        jdbcTemplate.queryForObject(
            "SELECT u.email FROM appointment a JOIN staff s ON s.staff_id = a.staff_id"
                + " JOIN app_user u ON u.user_id = s.user_id WHERE a.appt_id = ?",
            String.class,
            appointmentId);
    String path = "/api/v1/appointments/" + appointmentId;

    assertThat(read(tokenFor("alex.morgan@example.com", UserRole.CUSTOMER), path).getStatus())
        .isEqualTo(200);
    assertThat(read(tokenFor(staffEmail, UserRole.STAFF), path).getStatus()).isEqualTo(200);
    assertThat(
            read(tokenFor("hannah.reid@example.com", UserRole.ORGANIZATION_OWNER), path)
                .getStatus())
        .isEqualTo(200);
    assertThat(read(tokenFor("sofia.martin@example.com", UserRole.CUSTOMER), path).getStatus())
        .isEqualTo(403);
  }

  @Test
  void mayaSeesHerDayInTimeOrderWithItsWorkingHoursAndCustomersCannot() throws Exception {
    long maya =
        jdbcTemplate.queryForObject(
            "SELECT s.staff_id FROM staff s JOIN app_user u ON u.user_id = s.user_id"
                + " WHERE u.email = 'maya.chen@example.com'",
            Long.class);
    LocalDate nextMonday = LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    String path = "/api/v1/staff/" + maya + "/agenda?date=" + nextMonday;

    MockHttpServletResponse agenda = read(tokenFor("maya.chen@example.com", UserRole.STAFF), path);

    assertThat(agenda.getStatus()).isEqualTo(200);
    String body = agenda.getContentAsString();
    assertThat((String) JsonPath.read(body, "$.workingHours.startTime")).isEqualTo("09:00");
    assertThat((String) JsonPath.read(body, "$.workingHours.breakStart")).isEqualTo("12:00");
    List<String> starts = JsonPath.read(body, "$.appointments[*].start");
    assertThat(starts).isSortedAccordingTo(byTime());
    assertThat(starts).allMatch(start -> start.startsWith(nextMonday.toString()));
    assertThat(read(tokenFor("alex.morgan@example.com", UserRole.CUSTOMER), path).getStatus())
        .isEqualTo(403);
  }

  private MockHttpServletResponse read(String token, String path) throws Exception {
    return mockMvc
        .perform(get(path).header("Authorization", "Bearer " + token))
        .andReturn()
        .getResponse();
  }

  private String tokenFor(String email, UserRole role) {
    long userId =
        jdbcTemplate.queryForObject(
            "SELECT user_id FROM app_user WHERE email = ?", Long.class, email);
    AccountResponse account =
        new AccountResponse(userId, "Demo Account", email, null, role, null, null);
    return TestTokens.issuer(Clock.systemUTC()).issue(account).value();
  }

  private static Comparator<String> byTime() {
    return Comparator.comparing(OffsetDateTime::parse);
  }
}
