package com.smartappointment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Browser sign-in flow against PostgreSQL: login, /me with the cookie, logout with CSRF. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AuthFlowIT {

  private static final String PASSWORD = "flow-test-password";

  @Test
  void signsInReadsTheAccountAndSignsOut(
      @Autowired MockMvc mockMvc,
      @Autowired AppUserRepository appUserRepository,
      @Autowired PasswordEncoder passwordEncoder)
      throws Exception {
    appUserRepository.save(
        new AppUser(
            "Flow Customer",
            "flow.customer@example.com",
            passwordEncoder.encode(PASSWORD),
            UserRole.CUSTOMER));

    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"flow.customer@example.com\",\"password\":\""
                            + PASSWORD
                            + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    Cookie accessCookie = login.getResponse().getCookie("sa_access");
    assertThat(accessCookie).isNotNull();
    assertThat(accessCookie.isHttpOnly()).isTrue();

    MvcResult me =
        mockMvc
            .perform(get("/api/v1/me").cookie(accessCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("flow.customer@example.com"))
            .andReturn();
    Cookie csrfCookie = me.getResponse().getCookie("XSRF-TOKEN");
    assertThat(csrfCookie).isNotNull();

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .cookie(accessCookie, csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue()))
        .andExpect(status().isNoContent());
  }

  @Test
  void rejectsAWrongPassword(@Autowired MockMvc mockMvc) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong-password\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Invalid e-mail or password."));
  }
}
