package com.smartappointment.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.common.config.RateLimitProperties;
import com.smartappointment.common.config.RateLimitProperties.Policy;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryRateLimiterTest {

  private final RateLimiter rateLimiter =
      new InMemoryRateLimiter(
          new RateLimitProperties(
              true, Map.of("auth", new Policy(2, 2), "default", new Policy(5, 5))));

  @Test
  void allowsRequestsUpToCapacityThenRejects() {
    assertThat(rateLimiter.tryConsume("ip:1", "auth").isAllowed()).isTrue();
    assertThat(rateLimiter.tryConsume("ip:1", "auth").isAllowed()).isTrue();

    RateLimitDecision third = rateLimiter.tryConsume("ip:1", "auth");

    assertThat(third.isAllowed()).isFalse();
    assertThat(third.retryAfterSeconds()).isPositive();
  }

  @Test
  void keepsSeparateBucketsPerClient() {
    rateLimiter.tryConsume("ip:1", "auth");
    rateLimiter.tryConsume("ip:1", "auth");

    assertThat(rateLimiter.tryConsume("ip:2", "auth").isAllowed()).isTrue();
  }

  @Test
  void fallsBackToDefaultPolicyForUnknownGroups() {
    for (int i = 0; i < 5; i++) {
      assertThat(rateLimiter.tryConsume("ip:1", "unknown").isAllowed()).isTrue();
    }
    assertThat(rateLimiter.tryConsume("ip:1", "unknown").isAllowed()).isFalse();
  }
}
