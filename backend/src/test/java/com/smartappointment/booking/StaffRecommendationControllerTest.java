package com.smartappointment.booking;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.AccessTokenCookies;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.StaffOptionResponse;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import com.smartappointment.common.web.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Contract operation listStaffRecommendations through the real security chain (signed-in). */
@WebMvcTest(StaffRecommendationController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class StaffRecommendationControllerTest {

  private static final AccountResponse CUSTOMER =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);
  private static final LocalDate MONDAY = LocalDate.of(2026, 11, 30);

  @Autowired private MockMvc mockMvc;
  @MockitoBean private StaffRecommendationService staffRecommendationService;

  @Test
  void returnsTheRankedOptionsInTheContractShape() throws Exception {
    when(staffRecommendationService.rankStaff(eq(1L), eq(MONDAY)))
        .thenReturn(
            List.of(
                new StaffOptionResponse(
                    1L,
                    "Maya Chen",
                    "Academic advisor",
                    1,
                    true,
                    "Shortest estimated wait",
                    OffsetDateTime.parse("2026-11-30T09:00+03:00"),
                    5,
                    3,
                    12,
                    4.7)));

    mockMvc
        .perform(signedIn(get("/api/v1/services/1/staff-recommendations?date=2026-11-30")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].staffId").value(1))
        .andExpect(jsonPath("$[0].rank").value(1))
        .andExpect(jsonPath("$[0].isRecommended").value(true))
        .andExpect(jsonPath("$[0].recommendationReason").value("Shortest estimated wait"))
        .andExpect(jsonPath("$[0].nextFreeSlot").value("2026-11-30T09:00:00+03:00"))
        .andExpect(jsonPath("$[0].estimatedWaitMinutes").value(5))
        .andExpect(jsonPath("$[0].bookedSlots").value(3))
        .andExpect(jsonPath("$[0].totalSlots").value(12))
        .andExpect(jsonPath("$[0].meanRating").value(4.7));
  }

  @Test
  void needsAToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/services/1/staff-recommendations?date=2026-11-30"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void needsADate() throws Exception {
    mockMvc
        .perform(signedIn(get("/api/v1/services/1/staff-recommendations")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void answers404ForAMissingService() throws Exception {
    when(staffRecommendationService.rankStaff(eq(9L), eq(MONDAY)))
        .thenThrow(new ResourceNotFoundException("Service", 9L));

    mockMvc
        .perform(signedIn(get("/api/v1/services/9/staff-recommendations?date=2026-11-30")))
        .andExpect(status().isNotFound());
  }

  private static MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(CUSTOMER).value();
    return request.header("Authorization", "Bearer " + token);
  }
}
