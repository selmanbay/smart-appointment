package com.smartappointment.common.ratelimit;

import com.smartappointment.common.web.ProblemResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects requests over the policy limit with 429 and an RFC 7807 body. Clients are identified by
 * user name when signed in, otherwise by IP address.
 */
public final class RateLimitFilter extends OncePerRequestFilter {

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
    response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
    ProblemResponses.write(
        response,
        HttpStatus.TOO_MANY_REQUESTS,
        "Rate limit exceeded. Retry after " + retryAfterSeconds + " seconds.");
  }
}
