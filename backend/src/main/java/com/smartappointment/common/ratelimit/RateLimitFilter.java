package com.smartappointment.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects requests over the policy limit with 429 and an RFC 7807 body. Clients are identified by
 * user name when signed in, otherwise by IP address.
 */
public final class RateLimitFilter extends OncePerRequestFilter {

  private static final String PROBLEM_BODY =
      "{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,"
          + "\"detail\":\"Rate limit exceeded. Retry after %d seconds.\"}";

  private final RateLimiter rateLimiter;
  private final RoutePolicyResolver policyResolver;

  public RateLimitFilter(RateLimiter rateLimiter, RoutePolicyResolver policyResolver) {
    this.rateLimiter = rateLimiter;
    this.policyResolver = policyResolver;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    RateLimitDecision decision =
        policyResolver
            .resolve(request.getRequestURI())
            .map(policyName -> rateLimiter.tryConsume(clientKey(request), policyName))
            .orElseGet(RateLimitDecision::allow);
    if (decision.isAllowed()) {
      chain.doFilter(request, response);
    } else {
      writeTooManyRequests(response, decision.retryAfterSeconds());
    }
  }

  private static String clientKey(HttpServletRequest request) {
    Principal principal = request.getUserPrincipal();
    return principal != null ? "user:" + principal.getName() : "ip:" + request.getRemoteAddr();
  }

  private static void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds)
      throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.getWriter().write(PROBLEM_BODY.formatted(retryAfterSeconds));
  }
}
