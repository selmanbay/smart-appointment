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
import org.testcontainers.junit.jupiter.Testcontainers;

/** listStaffRecommendations end to end against the demo data (db/seed) in PostgreSQL. */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class StaffRecommendationsIT {

  private static final AccountResponse ALEX =
      new AccountResponse(
          1L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void ranksBothAdvisorsForNextMondayAndRecommendsTheFirst() throws Exception {
    LocalDate nextMonday =
        LocalDate.now(ZoneId.of("Europe/Istanbul")).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    Long advising =
        jdbcTemplate.queryForObject(
            "SELECT service_id FROM service WHERE name = 'Academic advising'", Long.class);
    String token = TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();

    String body =
        mockMvc
            .perform(
                get("/api/v1/services/{serviceId}/staff-recommendations", advising)
                    .param("date", nextMonday.toString())
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<String> names = JsonPath.read(body, "$[*].fullName");
    List<Integer> ranks = JsonPath.read(body, "$[*].rank");
    List<Boolean> recommended = JsonPath.read(body, "$[*].isRecommended");
    List<Integer> waits = JsonPath.read(body, "$[*].estimatedWaitMinutes");
    List<Integer> totalSlots = JsonPath.read(body, "$[*].totalSlots");
    List<Object> ratings = JsonPath.read(body, "$[*].meanRating");
    assertThat(names).containsExactlyInAnyOrder("Maya Chen", "Daniel Brooks");
    assertThat(ranks).containsExactly(1, 2);
    assertThat(recommended).containsExactly(true, false);
    assertThat(waits.get(0)).isLessThanOrEqualTo(waits.get(1));
    assertThat(totalSlots).containsOnly(12);
    assertThat(ratings).doesNotContainNull();
    assertThat((String) JsonPath.read(body, "$[0].recommendationReason"))
        .isEqualTo("Shortest estimated wait");
  }
}
