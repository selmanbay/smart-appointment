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
import com.smartappointment.booking.dto.SlotResponse;
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

/** Contract operation listFreeSlots through the real security chain (signed-in). */
@WebMvcTest(SlotController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class SlotControllerTest {

  private static final AccountResponse CUSTOMER =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);
  private static final LocalDate MONDAY = LocalDate.of(2026, 11, 30);

  @Autowired private MockMvc mockMvc;
  @MockitoBean private SlotService slotService;

  @Test
  void listFreeSlotsReturnsTimesWithTheOrganizationsOffset() throws Exception {
    when(slotService.freeSlots(eq(1L), eq(2L), eq(MONDAY)))
        .thenReturn(
            List.of(
                new SlotResponse(
                    OffsetDateTime.parse("2026-11-30T09:30+03:00"),
                    OffsetDateTime.parse("2026-11-30T10:00+03:00"),
                    1,
                    15)));

    mockMvc
        .perform(signedIn(get("/api/v1/staff/1/slots?serviceId=2&date=2026-11-30")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].start").value("2026-11-30T09:30:00+03:00"))
        .andExpect(jsonPath("$[0].end").value("2026-11-30T10:00:00+03:00"))
        .andExpect(jsonPath("$[0].remainingCapacity").value(1))
        .andExpect(jsonPath("$[0].estimatedWaitMinutes").value(15));
  }

  @Test
  void listFreeSlotsNeedsAToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/staff/1/slots?serviceId=2&date=2026-11-30"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void listFreeSlotsNeedsTheServiceAndADate() throws Exception {
    mockMvc
        .perform(signedIn(get("/api/v1/staff/1/slots?date=2026-11-30")))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(signedIn(get("/api/v1/staff/1/slots?serviceId=2&date=30.11.2026")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void listFreeSlotsAnswers404ForAStaffMemberWhoDoesNotProvideTheService() throws Exception {
    when(slotService.freeSlots(eq(1L), eq(3L), eq(MONDAY)))
        .thenThrow(new ResourceNotFoundException("Active staff member providing service 3", 1L));

    mockMvc
        .perform(signedIn(get("/api/v1/staff/1/slots?serviceId=3&date=2026-11-30")))
        .andExpect(status().isNotFound());
  }

  private static MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(CUSTOMER).value();
    return request.header("Authorization", "Bearer " + token);
  }
}
