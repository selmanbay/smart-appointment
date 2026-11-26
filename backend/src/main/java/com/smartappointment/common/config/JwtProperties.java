package com.smartappointment.common.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Signing and cookie settings for access tokens (ADR-0015). The secret comes from JWT_SECRET; the
 * application refuses to start with a missing or short secret.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret, String issuer, Duration lifetime, String cookieName, boolean isCookieSecure) {

  /** HS256 needs a key of at least 256 bits. */
  public static final int MIN_SECRET_BYTES = 32;

  public JwtProperties {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "app.jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_BYTES + " bytes");
    }
    if (lifetime == null || lifetime.isNegative() || lifetime.isZero()) {
      throw new IllegalStateException("app.jwt.lifetime must be positive");
    }
  }
}
