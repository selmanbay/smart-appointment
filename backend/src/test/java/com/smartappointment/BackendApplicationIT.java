package com.smartappointment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Starts the whole application against PostgreSQL and checks that Flyway created the schema. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class BackendApplicationIT {

  private static final List<String> SRS_TABLES =
      List.of(
          "organization",
          "app_user",
          "staff",
          "service",
          "staff_service",
          "appointment_rule",
          "working_hours",
          "schedule_change_request",
          "appointment",
          "notification",
          "feedback");

  @Test
  void migratesAllSrsTables(@Autowired JdbcTemplate jdbcTemplate) {
    List<String> tableNames =
        jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
            String.class);

    assertThat(tableNames).containsAll(SRS_TABLES);
  }
}
