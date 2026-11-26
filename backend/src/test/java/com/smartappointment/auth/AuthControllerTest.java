package com.smartappointment.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.auth.dto.AuthResponse;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Login, logout and /me through the real security chain (ADR-0015). */
@WebMvcTest({AuthController.class, AccountController.class})
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class AuthControllerTest {

  private static final AccountResponse ALEX =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);
  private static final String LOGIN_BODY =
      "{\"email\":\"alex.morgan@example.com\",\"password\":\"demo-password-2026\"}";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthService authService;
  @MockitoBean private AccountService accountService;

  @Test
  void loginReturnsTheTokenAndSetsAnHttpOnlyStrictCookie() throws Exception {
    when(authService.login(any()))
        .thenReturn(new AuthResponse("token-value", "Bearer", 28800, ALEX));

    mockMvc
        .perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.account.email").value("alex.morgan@example.com"))
        .andExpect(header().string("Set-Cookie", containsString("sa_access=token-value")))
        .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
        .andExpect(header().string("Set-Cookie", containsString("Path=/api")))
        .andExpect(header().string("Set-Cookie", containsString("Max-Age=28800")));
  }

  @Test
  void loginIgnoresAStaleAccessCookie() throws Exception {
    when(authService.login(any()))
        .thenReturn(new AuthResponse("token-value", "Bearer", 28800, ALEX));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .cookie(new Cookie("sa_access", "signed-with-an-old-secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
        .andExpect(status().isOk());
  }

  @Test
  void loginRejectsAMalformedBody() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void loginAnswersWrongCredentialsWithAProblem() throws Exception {
    when(authService.login(any()))
        .thenThrow(new BadCredentialsException("Invalid e-mail or password."));

    mockMvc
        .perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Invalid e-mail or password."))
        .andExpect(cookie().doesNotExist("sa_access"));
  }

  @Test
  void logoutNeedsTheCsrfToken() throws Exception {
    mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isForbidden());
  }

  @Test
  void logoutWorksWithTheCsrfTokenTheBrowserReceived() throws Exception {
    when(accountService.currentAccount(9L)).thenReturn(ALEX);
    Cookie accessCookie = new Cookie("sa_access", validToken());
    Cookie csrfCookie =
        mockMvc
            .perform(get("/api/v1/me").cookie(accessCookie))
            .andReturn()
            .getResponse()
            .getCookie("XSRF-TOKEN");

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .cookie(accessCookie, csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue()))
        .andExpect(status().isNoContent())
        .andExpect(header().string("Set-Cookie", containsString("sa_access=")))
        .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
  }

  @Test
  void rejectsACookieRequestWithAWrongCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .cookie(new Cookie("sa_access", validToken()), new Cookie("XSRF-TOKEN", "a"))
                .header("X-XSRF-TOKEN", "b"))
        .andExpect(status().isForbidden());
  }

  @Test
  void bearerRequestsNeedNoCsrfToken() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + validToken()))
        .andExpect(status().isNoContent());
  }

  @Test
  void meNeedsAToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("Content-Type", containsString("application/problem+json")));
  }

  @Test
  void meAcceptsABearerHeader() throws Exception {
    when(accountService.currentAccount(9L)).thenReturn(ALEX);

    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer " + validToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Alex Morgan"));
  }

  @Test
  void meAcceptsTheAccessCookieAndHandsOutACsrfCookie() throws Exception {
    when(accountService.currentAccount(9L)).thenReturn(ALEX);

    mockMvc
        .perform(get("/api/v1/me").cookie(new Cookie("sa_access", validToken())))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("XSRF-TOKEN"))
        .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
  }

  @Test
  void meAnswers401ForAStaleCookie() throws Exception {
    mockMvc
        .perform(get("/api/v1/me").cookie(new Cookie("sa_access", "signed-with-an-old-secret")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meRejectsATamperedToken() throws Exception {
    String token = validToken();
    String tampered = token.substring(0, token.length() - 4) + "AAAA";

    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer " + tampered))
        .andExpect(status().isUnauthorized());
  }

  private static String validToken() {
    return TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();
  }
}
