package com.smartappointment.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AccessCookieAuthenticationFilterTest {

  private final AccessCookieAuthenticationFilter filter =
      new AccessCookieAuthenticationFilter("sa_access");

  @Test
  void presentsTheCookieTokenAsABearerHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/me");
    request.setCookies(new Cookie("sa_access", "cookie-token"));

    assertThat(authorizationSeenDownstream(request)).isEqualTo("Bearer cookie-token");
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
