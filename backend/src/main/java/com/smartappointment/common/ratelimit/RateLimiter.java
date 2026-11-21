package com.smartappointment.common.ratelimit;

/**
 * Decides whether a client may make another request. The in-memory implementation is enough for a
 * single instance; a Redis-backed one replaces it when the backend is scaled out (ADR-0008).
 */
public interface RateLimiter {

  RateLimitDecision tryConsume(String clientKey, String policyName);
}
