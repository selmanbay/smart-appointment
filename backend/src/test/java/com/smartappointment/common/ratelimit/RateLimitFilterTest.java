package com.smartappointment.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

  private static final long RETRY_AFTER_SECONDS = 30;

  @Test
  void passesAllowedRequestsThrough() throws Exception {
    MockFilterChain chain = new MockFilterChain();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filterReturning(RateLimitDecision.allow())
        .doFilter(request("/api/v1/services"), response, chain);

    assertThat(chain.getRequest()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void rejectsLimitedRequestsWithProblemDetail() throws Exception {
    MockFilterChain chain = new MockFilterChain();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filterReturning(RateLimitDecision.reject(RETRY_AFTER_SECONDS))
        .doFilter(request("/api/v1/auth/login"), response, chain);

    assertThat(chain.getRequest()).isNull();
    assertThat(response.getStatus()).isEqualTo(429);
    assertThat(response.getHeader("Retry-After")).isEqualTo("30");
    assertThat(response.getContentType()).isEqualTo("application/problem+json");
    assertThat(response.getContentAsString()).contains("\"status\":429");
  }

  @Test
  void ignoresPathsOutsideTheApi() throws Exception {
    MockFilterChain chain = new MockFilterChain();

    filterReturning(RateLimitDecision.reject(RETRY_AFTER_SECONDS))
        .doFilter(request("/actuator/health"), new MockHttpServletResponse(), chain);

    assertThat(chain.getRequest()).isNotNull();
  }

  private static RateLimitFilter filterReturning(RateLimitDecision decision) {
    return new RateLimitFilter((clientKey, policyName) -> decision, new RoutePolicyResolver());
  }

  private static MockHttpServletRequest request(String path) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setRequestURI(path);
    return request;
  }
}
