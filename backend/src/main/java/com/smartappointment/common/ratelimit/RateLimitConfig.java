package com.smartappointment.common.ratelimit;

import com.smartappointment.common.config.RateLimitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers rate limiting only when app.rate-limit.enabled is true. */
@Configuration
@ConditionalOnProperty(prefix = "app.rate-limit", name = "enabled", havingValue = "true")
public class RateLimitConfig {

  @Bean
  RateLimiter rateLimiter(RateLimitProperties properties) {
    return new InMemoryRateLimiter(properties);
  }

  @Bean
  FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimiter rateLimiter) {
    FilterRegistrationBean<RateLimitFilter> registration =
        new FilterRegistrationBean<>(new RateLimitFilter(rateLimiter, new RoutePolicyResolver()));
    registration.addUrlPatterns("/api/*");
    return registration;
  }
}
