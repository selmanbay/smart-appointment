package com.smartappointment.organization;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.auth.AccessTokenCookies;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.dto.OrganizationResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Contract operation getOrganization through the real security chain: public, no token. */
@WebMvcTest(OrganizationController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class OrganizationControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private OrganizationService organizationService;

  @Test
  void getOrganizationNeedsNoSignIn() throws Exception {
    when(organizationService.getOrganization(eq(1L)))
        .thenReturn(
            new OrganizationResponse(
                1L, "Northbridge University", null, "Europe/Istanbul", null, Map.of(), null, true));

    mockMvc
        .perform(get("/api/v1/organizations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Northbridge University"));
  }

  @Test
  void getOrganizationAnswers404WhenMissing() throws Exception {
    when(organizationService.getOrganization(eq(404L)))
        .thenThrow(new ResourceNotFoundException("Organization", 404L));

    mockMvc.perform(get("/api/v1/organizations/404")).andExpect(status().isNotFound());
  }
}
