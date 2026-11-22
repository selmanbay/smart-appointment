package com.smartappointment.schedule;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.AccessTokenCookies;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Contract operations getAppointmentRule (public) and getWorkingHours (signed-in). */
@WebMvcTest(ScheduleController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class ScheduleControllerTest {

  private static final AccountResponse CUSTOMER =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ScheduleService scheduleService;

  @Test
  void getAppointmentRuleNeedsNoSignIn() throws Exception {
    when(scheduleService.getAppointmentRule(eq(1L)))
        .thenReturn(new AppointmentRuleResponse(30, 2, 24, 3));

    mockMvc
        .perform(get("/api/v1/services/1/rule"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maxAdvanceDays").value(30));
  }

  @Test
  void getAppointmentRuleAnswers404WhenTheServiceHasNoRule() throws Exception {
    when(scheduleService.getAppointmentRule(eq(2L)))
        .thenThrow(new ResourceNotFoundException("AppointmentRule for service", 2L));

    mockMvc.perform(get("/api/v1/services/2/rule")).andExpect(status().isNotFound());
  }

  @Test
  void getWorkingHoursNeedsAToken() throws Exception {
    mockMvc.perform(get("/api/v1/staff/1/working-hours")).andExpect(status().isUnauthorized());
  }

  @Test
  void getWorkingHoursAcceptsABearerToken() throws Exception {
    when(scheduleService.getWorkingHours(eq(1L)))
        .thenReturn(
            List.of(new WorkingHoursResponse((short) 1, "09:00", "16:00", "12:00", "13:00")));

    mockMvc
        .perform(
            get("/api/v1/staff/1/working-hours").header("Authorization", "Bearer " + validToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].startTime").value("09:00"));
  }

  @Test
  void getWorkingHoursAnswers404WhenTheStaffMemberIsMissing() throws Exception {
    when(scheduleService.getWorkingHours(eq(404L)))
        .thenThrow(new ResourceNotFoundException("Staff", 404L));

    mockMvc
        .perform(
            get("/api/v1/staff/404/working-hours")
                .header("Authorization", "Bearer " + validToken()))
        .andExpect(status().isNotFound());
  }

  private static String validToken() {
    return TestTokens.issuer(Clock.systemUTC()).issue(CUSTOMER).value();
  }
}
