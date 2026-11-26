package com.smartappointment.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import com.smartappointment.common.web.BusinessRuleException;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Contract operation bookAppointment through the real security chain and error handling. */
@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class BookingControllerTest {

  private static final AccountResponse ALEX =
      new AccountResponse(
          9L, "Alex Morgan", "alex.morgan@example.com", null, UserRole.CUSTOMER, null, null);
  private static final String BODY =
      "{\"serviceId\":1,\"staffId\":2,\"start\":\"2026-11-30T09:30:00+03:00\"}";
  private static final OffsetDateTime NINE_THIRTY = OffsetDateTime.parse("2026-11-30T09:30+03:00");

  @Autowired private MockMvc mockMvc;
  @MockitoBean private BookingService bookingService;

  @Test
  void aBookingAnswers201WithTheAppointmentAndItsLocation() throws Exception {
    when(bookingService.book(eq(9L), any(), isNull())).thenReturn(appointment());

    mockMvc
        .perform(signedIn(post("/api/v1/appointments")).content(BODY))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/appointments/42"))
        .andExpect(jsonPath("$.appointmentId").value(42))
        .andExpect(jsonPath("$.staff.fullName").value("Maya Chen"))
        .andExpect(jsonPath("$.start").value("2026-11-30T09:30:00+03:00"))
        .andExpect(jsonPath("$.status").value("BOOKED"))
        .andExpect(jsonPath("$.estimatedWaitMinutes").value(5))
        .andExpect(jsonPath("$.canCancel").value(true));
  }

  @Test
  void theIdempotencyKeyIsPassedOn() throws Exception {
    UUID key = UUID.fromString("7d0f4c58-4c2f-4d8e-9a3e-0f6b8a1c2d3e");
    when(bookingService.book(eq(9L), any(), eq(key))).thenReturn(appointment());

    mockMvc
        .perform(
            signedIn(post("/api/v1/appointments")).header("Idempotency-Key", key).content(BODY))
        .andExpect(status().isCreated());
  }

  @Test
  void aBrokenRuleAnswers409WithItsCode() throws Exception {
    when(bookingService.book(eq(9L), any(), isNull()))
        .thenThrow(
            new BusinessRuleException(
                "SLOT_TAKEN", "Slot no longer available", "Someone else booked this slot."));

    mockMvc
        .perform(signedIn(post("/api/v1/appointments")).content(BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SLOT_TAKEN"))
        .andExpect(jsonPath("$.title").value("Slot no longer available"))
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void aBodyWithoutTheStartIsRejected() throws Exception {
    mockMvc
        .perform(signedIn(post("/api/v1/appointments")).content("{\"serviceId\":1,\"staffId\":2}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void bookingNeedsSignIn() throws Exception {
    // A browser request passes the CSRF check (cookie and header match) but has no access cookie.
    mockMvc
        .perform(
            post("/api/v1/appointments")
                .cookie(new Cookie("XSRF-TOKEN", "csrf-value"))
                .header("X-XSRF-TOKEN", "csrf-value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void aBrowserRequestWithoutTheCsrfHeaderIsForbidden() throws Exception {
    mockMvc
        .perform(post("/api/v1/appointments").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isForbidden());
  }

  private static MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();
    return request
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON);
  }

  private static AppointmentResponse appointment() {
    return new AppointmentResponse(
        42L,
        1L,
        new ServiceReference(1L, "Academic advising"),
        new StaffReference(2L, "Maya Chen"),
        new CustomerReference(9L, "Alex Morgan"),
        NINE_THIRTY,
        NINE_THIRTY.plusMinutes(30),
        AppointmentStatus.BOOKED,
        5L,
        NINE_THIRTY.minusDays(1),
        null,
        null,
        null,
        null,
        true,
        false);
  }
}
