package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * UC-4 and UC-5 end to end against PostgreSQL and the demo data. Uses next Thursday so the other
 * demo-data tests (Monday, Wednesday) are not affected.
 */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class RescheduleAndCancelIT {

  private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void rescheduleKeepsTheOriginalWhenTheNewSlotIsTakenAndMovesItWhenFree() throws Exception {
    long maya = staffId();
    List<String> freeSlots = freeSlots(maya);
    String original = freeSlots.get(0);
    String taken = freeSlots.get(1);
    String free = freeSlots.get(2);
    String alex = tokenFor(newCustomer("Reschedule Customer"));
    long appointmentId = idOf(book(alex, maya, original));
    book(tokenFor(newCustomer("Other Customer")), maya, taken);

    MockHttpServletResponse refused = reschedule(alex, appointmentId, maya, taken);

    assertThat(refused.getStatus()).isEqualTo(409);
    assertThat((String) JsonPath.read(refused.getContentAsString(), "$.code"))
        .isEqualTo("SLOT_TAKEN");
    assertThat(stored(appointmentId)).containsEntry("status", "BOOKED");
    assertThat(isStoredAt(appointmentId, original)).isTrue();

    MockHttpServletResponse moved = reschedule(alex, appointmentId, maya, free);

    assertThat(moved.getStatus()).isEqualTo(200);
    assertThat(isStoredAt(appointmentId, free)).isTrue();
    assertThat(freeSlots(maya)).contains(original).doesNotContain(free, taken);
  }

  @Test
  void theCustomerCancelsOnceAndNobodyElseCan() throws Exception {
    long maya = staffId();
    String start = freeSlots(maya).getLast();
    String alex = tokenFor(newCustomer("Cancel Customer"));
    long appointmentId = idOf(book(alex, maya, start));

    assertThat(cancel(tokenFor(newCustomer("Stranger")), appointmentId).getStatus()).isEqualTo(403);
    MockHttpServletResponse cancelled = cancel(alex, appointmentId);

    assertThat(cancelled.getStatus()).isEqualTo(200);
    assertThat((String) JsonPath.read(cancelled.getContentAsString(), "$.status"))
        .isEqualTo("CANCELLED");
    assertThat(stored(appointmentId)).containsEntry("status", "CANCELLED");
    assertThat(stored(appointmentId).get("cancelled_at")).isNotNull();
    assertThat(freeSlots(maya)).contains(start);
    MockHttpServletResponse again = cancel(alex, appointmentId);
    assertThat(again.getStatus()).isEqualTo(409);
    assertThat((String) JsonPath.read(again.getContentAsString(), "$.code"))
        .isEqualTo("INVALID_STATUS");
  }

  private MockHttpServletResponse book(String token, long staffId, String start) throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"serviceId\":%d,\"staffId\":%d,\"start\":\"%s\"}"
                        .formatted(serviceId(), staffId, start)))
        .andReturn()
        .getResponse();
  }

  private MockHttpServletResponse reschedule(
      String token, long appointmentId, long staffId, String start) throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/appointments/{id}/reschedule", appointmentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"staffId\":%d,\"start\":\"%s\"}".formatted(staffId, start)))
        .andReturn()
        .getResponse();
  }

  private MockHttpServletResponse cancel(String token, long appointmentId) throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/appointments/{id}/cancel", appointmentId)
                .header("Authorization", "Bearer " + token))
        .andReturn()
        .getResponse();
  }

  private List<String> freeSlots(long staffId) throws Exception {
    LocalDate nextThursday =
        LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
    String body =
        mockMvc
            .perform(
                get("/api/v1/staff/{staffId}/slots", staffId)
                    .param("serviceId", String.valueOf(serviceId()))
                    .param("date", nextThursday.toString())
                    .header("Authorization", "Bearer " + tokenFor(1L)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(body, "$[*].start");
  }

  private static long idOf(MockHttpServletResponse response) throws Exception {
    assertThat(response.getStatus()).isEqualTo(201);
    return ((Number) JsonPath.read(response.getContentAsString(), "$.appointmentId")).longValue();
  }

  private Map<String, Object> stored(long appointmentId) {
    return jdbcTemplate.queryForMap(
        "SELECT status, cancelled_at FROM appointment WHERE appt_id = ?", appointmentId);
  }

  private boolean isStoredAt(long appointmentId, String start) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT start_time = ?::timestamptz FROM appointment WHERE appt_id = ?",
            Boolean.class,
            start,
            appointmentId));
  }

  private long newCustomer(String fullName) {
    String email =
        fullName.toLowerCase().replace(' ', '.') + "." + UUID.randomUUID() + "@example.com";
    return jdbcTemplate.queryForObject(
        "INSERT INTO app_user (full_name, email, password_hash, role)"
            + " VALUES (?, ?, '{bcrypt}not-used', 'CUSTOMER') RETURNING user_id",
        Long.class,
        fullName,
        email);
  }

  private static String tokenFor(long userId) {
    AccountResponse customer =
        new AccountResponse(
            userId, "Test Customer", "test@example.com", null, UserRole.CUSTOMER, null, null);
    return TestTokens.issuer(Clock.systemUTC()).issue(customer).value();
  }

  private long staffId() {
    return jdbcTemplate.queryForObject(
        "SELECT s.staff_id FROM staff s JOIN app_user u ON u.user_id = s.user_id"
            + " WHERE u.email = 'maya.chen@example.com'",
        Long.class);
  }

  private long serviceId() {
    return jdbcTemplate.queryForObject(
        "SELECT service_id FROM service WHERE name = 'Academic advising'", Long.class);
  }
}
