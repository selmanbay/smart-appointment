package com.smartappointment.booking;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.AccessTokenCookies;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AgendaResponse;
import com.smartappointment.booking.dto.AppointmentPageResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.AppointmentResponse.CustomerReference;
import com.smartappointment.booking.dto.AppointmentResponse.ServiceReference;
import com.smartappointment.booking.dto.AppointmentResponse.StaffReference;
import com.smartappointment.booking.dto.AppointmentScope;
import com.smartappointment.common.config.CorsProperties;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.CorsConfig;
import com.smartappointment.common.security.JwtConfig;
import com.smartappointment.common.security.SecurityConfig;
import com.smartappointment.common.web.ForbiddenException;
import com.smartappointment.common.web.PageMetadata;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
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

/** Contract operations getAppointment, listMyAppointments and getStaffAgenda (signed-in). */
@WebMvcTest({AppointmentQueryController.class, AgendaController.class})
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, AccessTokenCookies.class})
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
class AppointmentQueryControllerTest {

  private static final AccountResponse ALEX =
      new AccountResponse(9L, "Alex Morgan", "a@example.com", null, UserRole.CUSTOMER, null, null);
  private static final OffsetDateTime NINE = OffsetDateTime.parse("2026-11-30T09:00+03:00");
  private static final LocalDate MONDAY = LocalDate.of(2026, 11, 30);

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AppointmentQueryService appointmentQueryService;
  @MockitoBean private MyAppointmentsService myAppointmentsService;
  @MockitoBean private AgendaService agendaService;

  @Test
  void getAppointmentReturnsTheAppointment() throws Exception {
    when(appointmentQueryService.getAppointment(42L, 9L)).thenReturn(appointment());

    mockMvc
        .perform(signedIn(get("/api/v1/appointments/42")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.appointmentId").value(42))
        .andExpect(jsonPath("$.estimatedWaitMinutes").value(5));
  }

  @Test
  void getAppointmentAnswers403And404() throws Exception {
    when(appointmentQueryService.getAppointment(43L, 9L))
        .thenThrow(new ForbiddenException("This appointment belongs to someone else."));
    when(appointmentQueryService.getAppointment(44L, 9L))
        .thenThrow(new ResourceNotFoundException("Appointment", 44L));

    mockMvc.perform(signedIn(get("/api/v1/appointments/43"))).andExpect(status().isForbidden());
    mockMvc.perform(signedIn(get("/api/v1/appointments/44"))).andExpect(status().isNotFound());
  }

  @Test
  void myAppointmentsArePagedAndUpcomingByDefault() throws Exception {
    when(myAppointmentsService.listMyAppointments(9L, AppointmentScope.UPCOMING, 0, 20))
        .thenReturn(
            new AppointmentPageResponse(List.of(appointment()), new PageMetadata(0, 20, 1, 1)));

    mockMvc
        .perform(signedIn(get("/api/v1/me/appointments")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].appointmentId").value(42))
        .andExpect(jsonPath("$.page.totalElements").value(1))
        .andExpect(jsonPath("$.page.totalPages").value(1));
  }

  @Test
  void myAppointmentsRejectAnUnknownScopeAndAPageLargerThan100() throws Exception {
    mockMvc
        .perform(signedIn(get("/api/v1/me/appointments?scope=SOON")))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(signedIn(get("/api/v1/me/appointments?size=101")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void theAgendaCarriesTheDayItsWorkingHoursAndAppointments() throws Exception {
    when(agendaService.getAgenda(1L, MONDAY, 9L))
        .thenReturn(
            new AgendaResponse(
                MONDAY,
                1L,
                new WorkingHoursResponse((short) 1, "09:00", "16:00", "12:00", "13:00"),
                List.of(appointment())));

    mockMvc
        .perform(signedIn(get("/api/v1/staff/1/agenda?date=2026-11-30")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value("2026-11-30"))
        .andExpect(jsonPath("$.workingHours.startTime").value("09:00"))
        .andExpect(jsonPath("$.appointments[0].start").value("2026-11-30T09:00:00+03:00"));
  }

  @Test
  void everyOperationNeedsSignIn() throws Exception {
    mockMvc.perform(get("/api/v1/appointments/42")).andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/v1/me/appointments")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/v1/staff/1/agenda?date=2026-11-30"))
        .andExpect(status().isUnauthorized());
  }

  private static MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
    String token = TestTokens.issuer(Clock.systemUTC()).issue(ALEX).value();
    return request.header("Authorization", "Bearer " + token);
  }

  private static AppointmentResponse appointment() {
    return new AppointmentResponse(
        42L,
        1L,
        new ServiceReference(1L, "Academic advising"),
        new StaffReference(1L, "Maya Chen"),
        new CustomerReference(9L, "Alex Morgan"),
        NINE,
        NINE.plusMinutes(30),
        AppointmentStatus.BOOKED,
        5L,
        NINE.minusDays(1),
        null,
        null,
        null,
        null,
        true,
        false);
  }
}
