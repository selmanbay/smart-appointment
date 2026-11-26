package com.smartappointment.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AccessCookieAuthenticationFilterTest {

  private static final AccountResponse ALEX =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);

  private final AccessCookieAuthenticationFilter filter =
      new AccessCookieAuthenticationFilter("sa_access", TestTokens.decoder());

  @Test
  void presentsTheCookieTokenAsABearerHeader() throws Exception {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
    request.setCookies(new Cookie("sa_access", token));

    assertThat(authorizationSeenDownstream(request)).isEqualTo("Bearer " + token);
  }

  @Test
  void ignoresACookieWhoseTokenDoesNotVerify() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
    request.setCookies(new Cookie("sa_access", "signed-with-an-old-secret"));

    assertThat(authorizationSeenDownstream(request)).isNull();
  }

  @Test
  void keepsAnExistingAuthorizationHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
    request.addHeader("Authorization", "Bearer header-token");
    request.setCookies(new Cookie("sa_access", "cookie-token"));

    assertThat(authorizationSeenDownstream(request)).isEqualTo("Bearer header-token");
  }

  @Test
  void leavesRequestsWithoutTheCookieAlone() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
    request.setCookies(new Cookie("sa_access", ""), new Cookie("other", "x"));

    assertThat(authorizationSeenDownstream(request)).isNull();
  }

  private String authorizationSeenDownstream(MockHttpServletRequest request) throws Exception {
    MockFilterChain chain = new MockFilterChain();
    filter.doFilter(request, new MockHttpServletResponse(), chain);
    return ((HttpServletRequest) chain.getRequest()).getHeader("Authorization");
  }
}
