package com.smartappointment.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RoutePolicyResolverTest {

  private final RoutePolicyResolver resolver = new RoutePolicyResolver();

  @ParameterizedTest
  @CsvSource({
    "/api/v1/auth/login, auth",
    "/api/v1/appointments, booking",
    "/api/v1/appointments/42/reschedule, booking",
    "/api/v1/services, default"
  })
  void mapsApiPathsToPolicies(String path, String expectedPolicy) {
    assertThat(resolver.resolve(path)).contains(expectedPolicy);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/actuator/health", "/favicon.ico"})
  void skipsNonApiPaths(String path) {
    assertThat(resolver.resolve(path)).isEmpty();
  }
}
