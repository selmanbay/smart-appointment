package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

/** listFreeSlots end to end against the demo data (db/seed) in PostgreSQL. */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class FreeSlotsIT {

  private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");
  private static final int SLOTS_IN_A_DEMO_DAY = 12;
  private static final AccountResponse ALEX =
      new AccountResponse(
          1L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void nextMondayOffersEveryAdvisingSlotThatMayaHasNotBooked() throws Exception {
    LocalDate nextMonday = LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    long maya = staffId("maya.chen@example.com");
    long advising = serviceId("Academic advising");

    String body =
        mockMvc
            .perform(signedIn(maya, advising, nextMonday))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<String> starts = JsonPath.read(body, "$[*].start");
    List<Integer> remainingCapacities = JsonPath.read(body, "$[*].remainingCapacity");
    assertThat(starts).hasSize(SLOTS_IN_A_DEMO_DAY - bookedOn(maya, nextMonday));
    assertThat(starts.getFirst())
        .isIn(nextMonday + "T09:00:00+03:00", nextMonday + "T09:30:00+03:00");
    assertThat(starts).allMatch(start -> start.endsWith("+03:00"));
    assertThat(starts)
        .doesNotContain(nextMonday + "T12:00:00+03:00", nextMonday + "T16:00:00+03:00");
    assertThat(remainingCapacities).containsOnly(1);
  }

  @Test
  void aStaffMemberWhoDoesNotProvideTheServiceIsNotFound() throws Exception {
    LocalDate nextMonday = LocalDate.now(ISTANBUL).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    mockMvc
        .perform(
            signedIn(staffId("maya.chen@example.com"), serviceId("Document request"), nextMonday))
        .andExpect(status().isNotFound());
  }

  private static MockHttpServletRequestBuilder signedIn(
      long staffId, long serviceId, LocalDate date) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();
    return get("/api/v1/staff/{staffId}/slots", staffId)
        .param("serviceId", String.valueOf(serviceId))
        .param("date", date.toString())
        .header("Authorization", "Bearer " + token);
  }

  private long staffId(String email) {
    return jdbcTemplate.queryForObject(
        "SELECT s.staff_id FROM staff s JOIN app_user u ON u.user_id = s.user_id WHERE u.email = ?",
        Long.class,
        email);
  }

  private long serviceId(String name) {
    return jdbcTemplate.queryForObject(
        "SELECT service_id FROM service WHERE name = ?", Long.class, name);
  }

  private int bookedOn(long staffId, LocalDate date) {
    return jdbcTemplate.queryForObject(
        "SELECT count(*) FROM appointment WHERE staff_id = ? AND status = 'BOOKED'"
            + " AND (start_time AT TIME ZONE 'Europe/Istanbul')::date = ?",
        Integer.class,
        staffId,
        date);
  }
}
