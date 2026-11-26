package com.smartappointment.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import com.smartappointment.TestTokens;
import com.smartappointment.TestcontainersConfiguration;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import java.time.Clock;
import java.time.DayOfWeek;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * UC-20 against PostgreSQL and the demo data, with the sender replaced: a booking whose e-mail
 * fails still succeeds and the attempt is recorded as FAILED (handoff B12, AGENT_RULES section 8).
 */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class NotificationIT {

  private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @MockitoBean private NotificationSender notificationSender;

  @Test
  void aBookingWithAFailingSenderStillSucceedsAndRecordsFailed() throws Exception {
    when(notificationSender.send(any()))
        .thenReturn(DeliveryResult.failed("Connection refused: localhost:1025"));
    String token = tokenFor(newCustomer());

    MockHttpServletResponse booked = book(token, nextTuesdaySlot(0));

    assertThat(booked.getStatus()).isEqualTo(201);
    long appointmentId = idOf(booked);
    assertThat(appointmentStatus(appointmentId)).isEqualTo("BOOKED");
    Map<String, Object> notification = onlyNotificationOf(appointmentId);
    assertThat(notification)
        .containsEntry("type", "CONFIRMATION")
        .containsEntry("channel", "EMAIL")
        .containsEntry("status", "FAILED")
        .containsEntry("provider_response", "Connection refused: localhost:1025");
    assertThat(notification.get("sent_at")).isNull();
  }

  @Test
  void aCancellationIsMailedAndRecordedAsSent() throws Exception {
    when(notificationSender.send(any())).thenReturn(DeliveryResult.delivered("250 OK"));
    String token = tokenFor(newCustomer());
    long appointmentId = idOf(book(token, nextTuesdaySlot(1)));

    MockHttpServletResponse cancelled =
        mockMvc
            .perform(
                post("/api/v1/appointments/{id}/cancel", appointmentId)
                    .header("Authorization", "Bearer " + token))
            .andReturn()
            .getResponse();

    assertThat(cancelled.getStatus()).isEqualTo(200);
    List<Map<String, Object>> notifications =
        jdbcTemplate.queryForList(
            "SELECT type, status, sent_at FROM notification WHERE appt_id = ? ORDER BY notif_id",
            appointmentId);
    assertThat(notifications)
        .extracting(row -> row.get("type"))
        .containsExactly("CONFIRMATION", "CANCELLATION");
    assertThat(notifications).extracting(row -> row.get("status")).containsOnly("SENT");
    assertThat(notifications.get(1).get("sent_at")).isNotNull();
  }

  private MockHttpServletResponse book(String token, String start) throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"serviceId\":%d,\"staffId\":%d,\"start\":\"%s\"}"
                        .formatted(advisingId(), danielId(), start)))
        .andReturn()
        .getResponse();
  }

  private String nextTuesdaySlot(int index) throws Exception {
    LocalDate nextTuesday = LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
    String body =
        mockMvc
            .perform(
                get("/api/v1/staff/{staffId}/slots", danielId())
                    .param("serviceId", String.valueOf(advisingId()))
                    .param("date", nextTuesday.toString())
                    .header("Authorization", "Bearer " + tokenFor(1L)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<String> starts = JsonPath.read(body, "$[*].start");
    return starts.get(starts.size() - 1 - index);
  }

  private Map<String, Object> onlyNotificationOf(long appointmentId) {
    return jdbcTemplate.queryForMap(
        "SELECT type, channel, status, sent_at, provider_response FROM notification"
            + " WHERE appt_id = ?",
        appointmentId);
  }

  private String appointmentStatus(long appointmentId) {
    return jdbcTemplate.queryForObject(
        "SELECT status FROM appointment WHERE appt_id = ?", String.class, appointmentId);
  }

  private static long idOf(MockHttpServletResponse response) throws Exception {
    return ((Number) JsonPath.read(response.getContentAsString(), "$.appointmentId")).longValue();
  }

  private long newCustomer() {
    return jdbcTemplate.queryForObject(
        "INSERT INTO app_user (full_name, email, password_hash, role)"
            + " VALUES ('Notice Customer', ?, '{bcrypt}not-used', 'CUSTOMER') RETURNING user_id",
        Long.class,
        "notice.customer." + UUID.randomUUID() + "@example.com");
  }

  private static String tokenFor(long userId) {
    AccountResponse customer =
        new AccountResponse(
            userId, "Test Customer", "test@example.com", null, UserRole.CUSTOMER, null, null);
    return TestTokens.issuer(Clock.systemUTC()).issue(customer).value();
  }

  private long danielId() {
    return jdbcTemplate.queryForObject(
        "SELECT s.staff_id FROM staff s JOIN app_user u ON u.user_id = s.user_id"
            + " WHERE u.email = 'daniel.brooks@example.com'",
        Long.class);
  }

  private long advisingId() {
    return jdbcTemplate.queryForObject(
        "SELECT service_id FROM service WHERE name = 'Academic advising'", Long.class);
  }
}
