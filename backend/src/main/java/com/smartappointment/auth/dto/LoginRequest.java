package com.smartappointment.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

/** Body of POST /api/v1/auth/login (contract: LoginRequest). */
public record LoginRequest(
    @NotBlank @Email @Size(max = 320) String email, @NotBlank @Size(max = 128) String password) {

  /** E-mail addresses are stored in lower case. */
  public String normalizedEmail() {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
