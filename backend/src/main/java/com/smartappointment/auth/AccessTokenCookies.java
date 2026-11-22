package com.smartappointment.auth;

import com.smartappointment.auth.dto.AuthResponse;
import com.smartappointment.common.config.JwtProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Builds the HttpOnly access cookie for browsers (ADR-0015). */
@Component
public class AccessTokenCookies {

  private static final String API_PATH = "/api";
  private static final String SAME_SITE = "Strict";

  private final JwtProperties properties;

  public AccessTokenCookies(JwtProperties properties) {
    this.properties = properties;
  }

  /** Set-Cookie value carrying the token from a successful sign-in. */
  public String signedIn(AuthResponse response) {
    return base(response.accessToken())
        .maxAge(Duration.ofSeconds(response.expiresInSeconds()))
        .build()
        .toString();
  }

  /** Set-Cookie value that removes the access cookie. */
  public String signedOut() {
    return base("").maxAge(Duration.ZERO).build().toString();
  }

  private ResponseCookie.ResponseCookieBuilder base(String value) {
    return ResponseCookie.from(properties.cookieName(), value)
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .sameSite(SAME_SITE)
        .path(API_PATH);
  }
}
