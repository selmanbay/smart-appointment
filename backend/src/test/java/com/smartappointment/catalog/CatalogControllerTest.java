package com.smartappointment.catalog;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.auth.AccessTokenCookies;
import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import com.smartappointment.common.web.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Contract operation listServices through the real security chain: public, no token. */
@WebMvcTest(CatalogController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class CatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private CatalogService catalogService;

  @Test
  void listServicesNeedsNoSignIn() throws Exception {
    when(catalogService.listActiveServices(eq(1L)))
        .thenReturn(
            List.of(new ServiceResponse(1L, 1L, "Academic advising", null, 30, null, 0, true)));

    mockMvc
        .perform(get("/api/v1/organizations/1/services"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Academic advising"));
  }

  @Test
  void listServicesAnswers404WhenTheOrganizationIsMissing() throws Exception {
    when(catalogService.listActiveServices(eq(404L)))
        .thenThrow(new ResourceNotFoundException("Organization", 404L));

    mockMvc.perform(get("/api/v1/organizations/404/services")).andExpect(status().isNotFound());
  }
}
