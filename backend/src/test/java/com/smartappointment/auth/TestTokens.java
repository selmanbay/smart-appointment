package com.smartappointment.auth;

import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.JwtConfig;
import java.time.Clock;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/** Real signing and verification with the test secret, for tests that need valid tokens. */
final class TestTokens {

  static final JwtProperties PROPERTIES =
      new JwtProperties(
          "test-secret-for-automated-tests-only-0123456789",
          "smart-appointment",
          Duration.ofHours(8),
          "sa_access",
          false);

  private static final JwtConfig CONFIG = new JwtConfig();
  private static final SecretKey KEY = CONFIG.accessTokenKey(PROPERTIES);

  private TestTokens() {}

  static AccessTokenService issuer(Clock clock) {
    return new AccessTokenService(CONFIG.jwtEncoder(KEY), PROPERTIES, clock);
  }

  static JwtDecoder decoder() {
    return CONFIG.jwtDecoder(KEY, PROPERTIES);
  }
}
