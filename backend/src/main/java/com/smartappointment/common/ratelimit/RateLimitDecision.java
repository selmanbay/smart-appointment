package com.smartappointment.common.ratelimit;

/** Result of asking the rate limiter for one request. */
public record RateLimitDecision(boolean isAllowed, long retryAfterSeconds) {

  public static RateLimitDecision allow() {
    return new RateLimitDecision(true, 0);
  }

  public static RateLimitDecision reject(long retryAfterSeconds) {
    return new RateLimitDecision(false, retryAfterSeconds);
  }
}
