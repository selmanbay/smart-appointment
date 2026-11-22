package com.smartappointment;

import com.smartappointment.auth.AccessTokenService;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.JwtConfig;
import java.time.Clock;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Real signing and verification with the test secret (application-test.yml). Any feature's test can
 * create a valid token: {@code TestTokens.issuer(Clock.systemUTC()).issue(account).value()}.
 */
public final class TestTokens {

  public static final JwtProperties PROPERTIES =
      new JwtProperties(
          "test-secret-for-automated-tests-only-0123456789",
          "smart-appointment",
          Duration.ofHours(8),
          "sa_access",
          false);

  private static final JwtConfig CONFIG = new JwtConfig();
  private static final SecretKey KEY = CONFIG.accessTokenKey(PROPERTIES);

  private TestTokens() {}

  public static AccessTokenService issuer(Clock clock) {
    return new AccessTokenService(CONFIG.jwtEncoder(KEY), PROPERTIES, clock);
  }

  public static JwtDecoder decoder() {
    return CONFIG.jwtDecoder(KEY, PROPERTIES);
  }
}
