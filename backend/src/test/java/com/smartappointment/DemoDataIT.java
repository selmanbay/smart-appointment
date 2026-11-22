package com.smartappointment;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Loads the local demo data (db/seed) into PostgreSQL and checks what the MVP demo relies on. */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class DemoDataIT {

  private static final String DEMO_ORGANIZATION = "Northbridge University · Student Services";

  @Test
  void createsTheDemoOrganizationAccountsAndCatalog(@Autowired JdbcTemplate jdbcTemplate) {
    assertThat(
            count(
                jdbcTemplate,
                "SELECT count(*) FROM organization WHERE name = ?",
                DEMO_ORGANIZATION))
        .isEqualTo(1);
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM app_user")).isEqualTo(9);
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM staff")).isEqualTo(2);
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM service")).isEqualTo(2);
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM appointment_rule")).isEqualTo(2);
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM working_hours")).isEqualTo(10);
  }

  @Test
  void createsHistoryWithRecordedServiceTimesAndUpcomingBookings(
      @Autowired JdbcTemplate jdbcTemplate) {
    assertThat(
            count(
                jdbcTemplate,
                "SELECT count(*) FROM appointment WHERE status = 'COMPLETED'"
                    + " AND service_start_at IS NOT NULL AND service_end_at > service_start_at"))
        .isPositive();
    assertThat(
            count(
                jdbcTemplate,
                "SELECT count(*) FROM appointment WHERE status = 'BOOKED' AND start_time > now()"))
        .isPositive();
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM feedback")).isPositive();
  }

  @Test
  void runningTheSeedAgainChangesNothing(@Autowired JdbcTemplate jdbcTemplate) throws Exception {
    long appointmentsBefore = count(jdbcTemplate, "SELECT count(*) FROM appointment");
    String seed =
        new ClassPathResource("db/seed/R__demo_data.sql")
            .getContentAsString(StandardCharsets.UTF_8);

    jdbcTemplate.execute(seed);

    assertThat(count(jdbcTemplate, "SELECT count(*) FROM appointment"))
        .isEqualTo(appointmentsBefore);
    assertThat(count(jdbcTemplate, "SELECT count(*) FROM organization")).isEqualTo(1);
  }

  private static long count(JdbcTemplate jdbcTemplate, String sql, Object... arguments) {
    Long result = jdbcTemplate.queryForObject(sql, Long.class, arguments);
    return result == null ? 0 : result;
  }
}
