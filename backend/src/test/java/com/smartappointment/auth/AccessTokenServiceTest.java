package com.smartappointment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smartappointment.auth.dto.AccountResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;

class AccessTokenServiceTest {

  private static final AccountResponse STAFF_ACCOUNT =
      new AccountResponse(7L, "Maya Chen", "maya.chen@example.com", null, UserRole.STAFF, 1L, 3L);

  @Test
  void issuesATokenWithTheAgreedClaims() {
    Instant now = Instant.now();
    AccessToken token = TestTokens.issuer(Clock.fixed(now, ZoneOffset.UTC)).issue(STAFF_ACCOUNT);

    Jwt jwt = TestTokens.decoder().decode(token.value());

    assertThat(jwt.getSubject()).isEqualTo("7");
    assertThat(jwt.getClaimAsString("role")).isEqualTo("STAFF");
    assertThat(jwt.getClaim("organizationId").toString()).isEqualTo("1");
    assertThat(jwt.getClaim("staffId").toString()).isEqualTo("3");
    assertThat(jwt.getClaimAsString("iss")).isEqualTo("smart-appointment");
    assertThat(token.lifetime()).isEqualTo(Duration.ofHours(8));
  }

  @Test
  void omitsOrganizationAndStaffForCustomers() {
    AccountResponse customer =
        new AccountResponse(
            9L, "Alex Morgan", "alex@example.com", null, UserRole.CUSTOMER, null, null);

    Jwt jwt =
        TestTokens.decoder().decode(TestTokens.issuer(Clock.systemUTC()).issue(customer).value());

    assertThat(jwt.hasClaim("organizationId")).isFalse();
    assertThat(jwt.hasClaim("staffId")).isFalse();
  }

  @Test
  void rejectsAnExpiredToken() {
    Instant longAgo = Instant.now().minus(Duration.ofHours(9));
    AccessToken token =
        TestTokens.issuer(Clock.fixed(longAgo, ZoneOffset.UTC)).issue(STAFF_ACCOUNT);

    assertThatThrownBy(() -> TestTokens.decoder().decode(token.value()))
        .isInstanceOf(JwtValidationException.class);
  }
}
