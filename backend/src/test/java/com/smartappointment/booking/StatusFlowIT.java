package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import com.smartappointment.TestTokens;
import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.estimation.LearnedDuration;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * UC-8 end to end against PostgreSQL and the demo data: check-in, start and completion with server
 * times, and the learned duration (Eq. 3) stored in the same transaction. Uses next Friday so the
 * other demo-data tests are not affected.
 */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class StatusFlowIT {

  private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void theStaffMemberRecordsTheVisitAndCompletionUpdatesTheLearnedDuration() throws Exception {
    long daniel = staffIdOf("daniel.brooks@example.com");
    long appointmentId = bookNextFridayWith(daniel);
    String danielToken = tokenFor(userIdOf("daniel.brooks@example.com"), UserRole.STAFF);
    BigDecimal learnedBefore = learnedDuration();

    assertThat(changeStatus(danielToken, appointmentId, "CHECKED_IN")).isEqualTo(200);
    assertThat(changeStatus(danielToken, appointmentId, "CHECKED_IN")).isEqualTo(409);
    assertThat(changeStatus(danielToken, appointmentId, "IN_SERVICE")).isEqualTo(200);
    assertThat(changeStatus(danielToken, appointmentId, "COMPLETED")).isEqualTo(200);

    Map<String, Object> stored =
        jdbcTemplate.queryForMap(
            "SELECT status, checked_in_at, service_start_at, service_end_at"
                + " FROM appointment WHERE appt_id = ?",
            appointmentId);
    assertThat(stored.get("status")).isEqualTo("COMPLETED");
    assertThat(stored.get("checked_in_at")).isNotNull();
    Duration measured =
        Duration.between(
            ((Timestamp) stored.get("service_start_at")).toInstant(),
            ((Timestamp) stored.get("service_end_at")).toInstant());
    BigDecimal expected = LearnedDuration.next(learnedBefore, 30, measured);
    assertThat(learnedDuration()).isEqualByComparingTo(expected);
  }

  @Test
  void anotherStaffMemberCannotRecordTheStatus() throws Exception {
    long appointmentId = bookNextFridayWith(staffIdOf("daniel.brooks@example.com"));
    String mayaToken = tokenFor(userIdOf("maya.chen@example.com"), UserRole.STAFF);

    assertThat(changeStatus(mayaToken, appointmentId, "CHECKED_IN")).isEqualTo(403);
  }

  private int changeStatus(String token, long appointmentId, String status) throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/appointments/{id}/status", appointmentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}"))
        .andReturn()
        .getResponse()
        .getStatus();
  }

  private long bookNextFridayWith(long staffId) throws Exception {
    String customerToken = tokenFor(newCustomer(), UserRole.CUSTOMER);
    LocalDate nextFriday = LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
    String slots =
        mockMvc
            .perform(
                get("/api/v1/staff/{staffId}/slots", staffId)
                    .param("serviceId", String.valueOf(advisingId()))
                    .param("date", nextFriday.toString())
                    .header("Authorization", "Bearer " + customerToken))
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<String> starts = JsonPath.read(slots, "$[*].start");
    String booked =
        mockMvc
            .perform(
                post("/api/v1/appointments")
                    .header("Authorization", "Bearer " + customerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"serviceId\":%d,\"staffId\":%d,\"start\":\"%s\"}"
                            .formatted(advisingId(), staffId, starts.getLast())))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return ((Number) JsonPath.read(booked, "$.appointmentId")).longValue();
  }

  private BigDecimal learnedDuration() {
    return jdbcTemplate.queryForObject(
        "SELECT learned_duration_min FROM service WHERE service_id = ?",
        BigDecimal.class,
        advisingId());
  }

  private long newCustomer() {
    return jdbcTemplate.queryForObject(
        "INSERT INTO app_user (full_name, email, password_hash, role)"
            + " VALUES ('Status Customer', ?, '{bcrypt}not-used', 'CUSTOMER') RETURNING user_id",
        Long.class,
        "status.customer." + UUID.randomUUID() + "@example.com");
  }

  private static String tokenFor(long userId, UserRole role) {
    AccountResponse account =
        new AccountResponse(userId, "Test Account", "test@example.com", null, role, null, null);
    return TestTokens.issuer(Clock.systemUTC()).issue(account).value();
  }

  private long userIdOf(String email) {
    return jdbcTemplate.queryForObject(
        "SELECT user_id FROM app_user WHERE email = ?", Long.class, email);
  }

  private long staffIdOf(String email) {
    return jdbcTemplate.queryForObject(
        "SELECT s.staff_id FROM staff s JOIN app_user u ON u.user_id = s.user_id WHERE u.email = ?",
        Long.class,
        email);
  }

  private long advisingId() {
    return jdbcTemplate.queryForObject(
        "SELECT service_id FROM service WHERE name = 'Academic advising'", Long.class);
  }
}
