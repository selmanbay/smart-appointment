package com.smartappointment.auth.dto;

/** Result of a successful sign-in (contract: AuthResponse). */
public record AuthResponse(
    String accessToken, String tokenType, long expiresInSeconds, AccountResponse account) {

  public static final String BEARER = "Bearer";
}
