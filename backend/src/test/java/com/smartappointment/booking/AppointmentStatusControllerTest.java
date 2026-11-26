package com.smartappointment.booking;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.AccessTokenCookies;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.AppointmentResponse.CustomerReference;
import com.smartappointment.booking.dto.AppointmentResponse.ServiceReference;
import com.smartappointment.booking.dto.AppointmentResponse.StaffReference;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import com.smartappointment.common.web.ForbiddenException;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Contract operation changeAppointmentStatus through the security chain and error handling. */
@WebMvcTest(AppointmentStatusController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class AppointmentStatusControllerTest {

  private static final AccountResponse MAYA =
      new AccountResponse(4L, "Maya Chen", "m@example.com", null, UserRole.STAFF, 1L, 1L);
  private static final OffsetDateTime NINE = OffsetDateTime.parse("2026-11-30T09:00+03:00");

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AppointmentStatusService appointmentStatusService;

  @Test
  void aRecordedCheckInAnswers200() throws Exception {
    when(appointmentStatusService.changeStatus(42L, 4L, AppointmentStatus.CHECKED_IN))
        .thenReturn(checkedIn());

    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/status")).content(body("CHECKED_IN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CHECKED_IN"))
        .andExpect(jsonPath("$.checkedInAt").value("2026-11-30T08:55:00+03:00"));
  }

  @Test
  void statusesStaffDoNotRecordAreABadRequest() throws Exception {
    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/status")).content(body("CANCELLED")))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/status")).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aTransitionSrs25DoesNotAllowAnswers409() throws Exception {
    when(appointmentStatusService.changeStatus(42L, 4L, AppointmentStatus.COMPLETED))
        .thenThrow(BookingProblem.INVALID_STATUS.exception());

    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/status")).content(body("COMPLETED")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS"));
  }

  @Test
  void someoneElsesAppointmentIsForbidden() throws Exception {
    when(appointmentStatusService.changeStatus(42L, 4L, AppointmentStatus.NO_SHOW))
        .thenThrow(new ForbiddenException("Only the appointment's staff member."));

    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/status")).content(body("NO_SHOW")))
        .andExpect(status().isForbidden());
  }

  private static String body(String status) {
    return "{\"status\":\"" + status + "\"}";
  }

  private static MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(MAYA).value();
    return request
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON);
  }

  private static AppointmentResponse checkedIn() {
    return new AppointmentResponse(
        42L,
        1L,
        new ServiceReference(1L, "Academic advising"),
        new StaffReference(1L, "Maya Chen"),
        new CustomerReference(9L, "Alex Morgan"),
        NINE,
        NINE.plusMinutes(30),
        AppointmentStatus.CHECKED_IN,
        null,
        NINE.minusDays(1),
        null,
        NINE.minusMinutes(5),
        null,
        null,
        false,
        false);
  }
}
