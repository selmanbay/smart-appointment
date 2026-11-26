package com.smartappointment.auth;

import java.time.Duration;

/** A signed access token and how long it is valid. */
public record AccessToken(String value, Duration lifetime) {}
