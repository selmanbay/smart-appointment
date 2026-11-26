package com.smartappointment.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

/** Contract operations rescheduleAppointment and cancelAppointment through the security chain. */
@WebMvcTest(AppointmentChangeController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class AppointmentChangeControllerTest {

  private static final AccountResponse ALEX =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);
  private static final OffsetDateTime TWO_PM = OffsetDateTime.parse("2026-11-30T14:00+03:00");

  @Autowired private MockMvc mockMvc;
  @MockitoBean private RescheduleService rescheduleService;
  @MockitoBean private CancellationService cancellationService;

  @Test
  void rescheduleAnswers200WithTheMovedAppointment() throws Exception {
    when(rescheduleService.reschedule(eq(42L), eq(9L), any()))
        .thenReturn(appointment(AppointmentStatus.BOOKED));

    mockMvc
        .perform(
            signedIn(post("/api/v1/appointments/42/reschedule"))
                .content("{\"staffId\":2,\"start\":\"2026-11-30T14:00:00+03:00\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.start").value("2026-11-30T14:00:00+03:00"))
        .andExpect(jsonPath("$.staff.staffId").value(2));
  }

  @Test
  void rescheduleNeedsTheStaffMemberAndTheStart() throws Exception {
    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/reschedule")).content("{\"staffId\":2}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aTakenSlotAnswers409WithItsCode() throws Exception {
    when(rescheduleService.reschedule(eq(42L), eq(9L), any()))
        .thenThrow(BookingProblem.SLOT_TAKEN.exception());

    mockMvc
        .perform(
            signedIn(post("/api/v1/appointments/42/reschedule"))
                .content("{\"staffId\":2,\"start\":\"2026-11-30T14:00:00+03:00\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SLOT_TAKEN"));
  }

  @Test
  void cancelAnswers200WithTheCancelledAppointment() throws Exception {
    when(cancellationService.cancel(42L, 9L)).thenReturn(appointment(AppointmentStatus.CANCELLED));

    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/cancel")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  void cancellingSomeoneElsesAppointmentIsForbidden() throws Exception {
    when(cancellationService.cancel(42L, 9L))
        .thenThrow(new ForbiddenException("Only the customer or the owner can cancel."));

    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/cancel")))
        .andExpect(status().isForbidden());
  }

  @Test
  void aPastDeadlineAnswers409WithItsCode() throws Exception {
    when(cancellationService.cancel(42L, 9L))
        .thenThrow(BookingProblem.CANCEL_DEADLINE_PASSED.exception());

    mockMvc
        .perform(signedIn(post("/api/v1/appointments/42/cancel")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CANCEL_DEADLINE_PASSED"));
  }

  private static MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();
    return request
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON);
  }

  private static AppointmentResponse appointment(AppointmentStatus status) {
    return new AppointmentResponse(
        42L,
        1L,
        new ServiceReference(1L, "Academic advising"),
        new StaffReference(2L, "Daniel Brooks"),
        new CustomerReference(9L, "Alex Morgan"),
        TWO_PM,
        TWO_PM.plusMinutes(30),
        status,
        null,
        TWO_PM.minusDays(2),
        null,
        null,
        null,
        null,
        false,
        false);
  }
}
