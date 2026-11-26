package com.smartappointment.common.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

  @Test
  void rejectsASecretShorterThan32Bytes() {
    assertThatThrownBy(
            () -> new JwtProperties("too-short", "issuer", Duration.ofHours(1), "sa_access", true))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT_SECRET");
  }

  @Test
  void rejectsAMissingSecret() {
    assertThatThrownBy(() -> new JwtProperties(null, "issuer", Duration.ofHours(1), "c", true))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void rejectsANonPositiveLifetime() {
    assertThatThrownBy(() ->
            new JwtProperties(
                "a-secret-that-is-long-enough-0123456789", "issuer", Duration.ZERO, "c", true))
        .isInstanceOf(IllegalStateException.class);
  }
}
