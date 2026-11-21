package com.smartappointment.common.ratelimit;

import com.smartappointment.common.config.RateLimitProperties;
import java.util.List;
import java.util.Optional;

/** Maps a request path to the rate limit policy group it belongs to. */
public final class RoutePolicyResolver {

  private static final String API_PREFIX = "/api/";

  private static final List<PathPolicy> PATH_POLICIES =
      List.of(
          new PathPolicy("/api/v1/auth/", "auth"),
          new PathPolicy("/api/v1/appointments", "booking"));

  /** Returns the policy name, or empty for paths that are not rate limited (health, docs). */
  public Optional<String> resolve(String path) {
    if (!path.startsWith(API_PREFIX)) {
      return Optional.empty();
    }
    String policyName =
        PATH_POLICIES.stream()
            .filter(pathPolicy -> path.startsWith(pathPolicy.pathPrefix()))
            .map(PathPolicy::policyName)
            .findFirst()
            .orElse(RateLimitProperties.DEFAULT_POLICY);
    return Optional.of(policyName);
  }

  private record PathPolicy(String pathPrefix, String policyName) {}
}
