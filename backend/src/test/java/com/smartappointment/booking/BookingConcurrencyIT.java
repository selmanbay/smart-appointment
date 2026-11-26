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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
 * bookAppointment under concurrency against PostgreSQL and the demo data (AGENT_RULES section 7):
 * parallel requests for the last free place, and a repeated Idempotency-Key. Uses next Wednesday so
 * the other demo-data tests (next Monday) are not affected.
 */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class BookingConcurrencyIT {

  private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");
  private static final int PARALLEL_REQUESTS = 8;

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void exactlyOneOfManyParallelRequestsGetsTheLastFreePlace() throws Exception {
    long maya = staffId("maya.chen@example.com");
    long advising = serviceId();
    String start = lastFreeSlot(maya, advising);
    List<Callable<MockHttpServletResponse>> requests = new ArrayList<>();
    for (int i = 0; i < PARALLEL_REQUESTS; i++) {
      String token = tokenFor(newCustomer("Parallel Customer " + i));
      String body = bookingBody(maya, advising, start);
      requests.add(() -> book(token, body, null));
    }

    List<MockHttpServletResponse> responses = runTogether(requests);

    List<Integer> statuses = responses.stream().map(MockHttpServletResponse::getStatus).toList();
    assertThat(statuses).containsOnly(201, 409);
    assertThat(statuses).filteredOn(status -> status == 201).hasSize(1);
    assertThat(responses)
        .filteredOn(response -> response.getStatus() == 409)
        .allMatch(response -> "SLOT_TAKEN".equals(code(response)));
    assertThat(bookedAt(maya, start)).isEqualTo(1);
  }

  @Test
  void aRepeatedIdempotencyKeyBooksOnceEvenWhenSentTwiceAtOnce() throws Exception {
    long daniel = staffId("daniel.brooks@example.com");
    long advising = serviceId();
    String start = lastFreeSlot(daniel, advising);
    String token = tokenFor(newCustomer("Double Click Customer"));
    UUID key = UUID.randomUUID();
    String body = bookingBody(daniel, advising, start);
    Callable<MockHttpServletResponse> request = () -> book(token, body, key);

    List<MockHttpServletResponse> responses = runTogether(List.of(request, request));

    assertThat(responses).allMatch(response -> response.getStatus() == 201);
    Object firstId = JsonPath.read(responses.get(0).getContentAsString(), "$.appointmentId");
    Object secondId = JsonPath.read(responses.get(1).getContentAsString(), "$.appointmentId");
    assertThat(secondId).isEqualTo(firstId);
    assertThat(bookedAt(daniel, start)).isEqualTo(1);
  }

  private MockHttpServletResponse book(String token, String body, UUID idempotencyKey)
      throws Exception {
    var request =
        post("/api/v1/appointments")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    if (idempotencyKey != null) {
      request.header("Idempotency-Key", idempotencyKey.toString());
    }
    return mockMvc.perform(request).andReturn().getResponse();
  }

  private static String bookingBody(long staffId, long serviceId, String start) {
    return "{\"serviceId\":%d,\"staffId\":%d,\"start\":\"%s\"}"
        .formatted(serviceId, staffId, start);
  }

  private static List<MockHttpServletResponse> runTogether(
      List<Callable<MockHttpServletResponse>> requests) throws Exception {
    CountDownLatch startTogether = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(requests.size());
    try {
      List<Future<MockHttpServletResponse>> futures = new ArrayList<>();
      for (Callable<MockHttpServletResponse> request : requests) {
        futures.add(
            executor.submit(() -> {
              startTogether.await();
              return request.call();
            }));
      }
      startTogether.countDown();
      List<MockHttpServletResponse> responses = new ArrayList<>();
      for (Future<MockHttpServletResponse> future : futures) {
        responses.add(future.get());
      }
      return responses;
    } finally {
      executor.shutdownNow();
    }
  }

  private String lastFreeSlot(long staffId, long serviceId) throws Exception {
    LocalDate nextWednesday =
        LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
    String body =
        mockMvc
            .perform(
                get("/api/v1/staff/{staffId}/slots", staffId)
                    .param("serviceId", String.valueOf(serviceId))
                    .param("date", nextWednesday.toString())
                    .header("Authorization", "Bearer " + tokenFor(1L)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<String> starts = JsonPath.read(body, "$[*].start");
    return starts.getLast();
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

  private long staffId(String email) {
    return jdbcTemplate.queryForObject(
        "SELECT s.staff_id FROM staff s JOIN app_user u ON u.user_id = s.user_id WHERE u.email = ?",
        Long.class,
        email);
  }

  private long serviceId() {
    return jdbcTemplate.queryForObject(
        "SELECT service_id FROM service WHERE name = 'Academic advising'", Long.class);
  }

  private int bookedAt(long staffId, String start) {
    return jdbcTemplate.queryForObject(
        "SELECT count(*) FROM appointment WHERE staff_id = ? AND status = 'BOOKED'"
            + " AND start_time = ?::timestamptz",
        Integer.class,
        staffId,
        start);
  }

  private static String code(MockHttpServletResponse response) {
    try {
      return JsonPath.read(response.getContentAsString(), "$.code");
    } catch (java.io.UnsupportedEncodingException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
