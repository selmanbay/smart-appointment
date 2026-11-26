package com.smartappointment.auth;

import com.smartappointment.auth.dto.AuthResponse;
import com.smartappointment.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contract operations login and logout (api/openapi.yaml, ADR-0015). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String SET_COOKIE = "Set-Cookie";

  private final AuthService authService;
  private final AccessTokenCookies accessTokenCookies;

  public AuthController(AuthService authService, AccessTokenCookies accessTokenCookies) {
    this.authService = authService;
    this.accessTokenCookies = accessTokenCookies;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok()
        .header(SET_COOKIE, accessTokenCookies.signedIn(response))
        .body(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().header(SET_COOKIE, accessTokenCookies.signedOut()).build();
  }
}
