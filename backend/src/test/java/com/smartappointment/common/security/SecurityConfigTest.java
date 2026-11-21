package com.smartappointment.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.common.config.CorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import(SecurityConfig.class)
@EnableConfigurationProperties(CorsProperties.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class SecurityConfigTest {

  private static final String WEB_ORIGIN = "http://localhost:5173";

  @Test
  void rejectsUnauthenticatedApiRequests(@Autowired MockMvc mockMvc) throws Exception {
    mockMvc.perform(get("/api/v1/services")).andExpect(status().isUnauthorized());
  }

  @Test
  void allowsCorsPreflightFromTheWebApp(@Autowired MockMvc mockMvc) throws Exception {
    mockMvc
        .perform(
            options("/api/v1/services")
                .header("Origin", WEB_ORIGIN)
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", WEB_ORIGIN));
  }

  @Test
  void rejectsCorsPreflightFromUnknownOrigins(@Autowired MockMvc mockMvc) throws Exception {
    mockMvc
        .perform(
            options("/api/v1/services")
                .header("Origin", "https://evil.example")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }
}
