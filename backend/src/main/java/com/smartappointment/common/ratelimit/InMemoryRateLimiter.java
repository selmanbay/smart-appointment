package com.smartappointment.common.ratelimit;

import com.smartappointment.common.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Token-bucket rate limiter kept in this JVM's memory. */
public final class InMemoryRateLimiter implements RateLimiter {

  private final RateLimitProperties properties;
  private final Map<String, Bucket> bucketsByKey = new ConcurrentHashMap<>();

  public InMemoryRateLimiter(RateLimitProperties properties) {
    this.properties = properties;
  }

  @Override
  public RateLimitDecision tryConsume(String clientKey, String policyName) {
    Bucket bucket =
        bucketsByKey.computeIfAbsent(policyName + ":" + clientKey, key -> newBucket(policyName));
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (probe.isConsumed()) {
      return RateLimitDecision.allow();
    }
    long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
    return RateLimitDecision.reject(Math.max(1, waitSeconds));
  }

  private Bucket newBucket(String policyName) {
    RateLimitProperties.Policy policy = properties.policyFor(policyName);
    return Bucket.builder()
        .addLimit(limit ->
            limit
                .capacity(policy.capacity())
                .refillGreedy(policy.refillPerMinute(), Duration.ofMinutes(1)))
        .build();
  }
}
