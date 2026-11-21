package com.smartappointment.common.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rate limit policies per route group (ADR-0008). Disabled locally; enabled with
 * RATE_LIMIT_ENABLED=true when hosted.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(boolean enabled, Map<String, Policy> policies) {

  public static final String DEFAULT_POLICY = "default";

  public RateLimitProperties {
    policies = policies == null ? Map.of() : Map.copyOf(policies);
  }

  public Policy policyFor(String policyName) {
    Policy policy = policies.getOrDefault(policyName, policies.get(DEFAULT_POLICY));
    if (policy == null) {
      throw new IllegalStateException("No rate limit policy named " + policyName + " or default");
    }
    return policy;
  }

  /** A token bucket: up to {@code capacity} requests, refilled per minute. */
  public record Policy(int capacity, int refillPerMinute) {}
}
