package com.smartappointment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartappointment.TestTokens;
import com.smartappointment.auth.dto.AuthResponse;
import com.smartappointment.auth.dto.LoginRequest;
import com.smartappointment.catalog.StaffLookupService;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AuthServiceTest {

  private static final String PASSWORD = "correct-horse-battery";

  private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
  private final StaffLookupService staffLookupService = mock(StaffLookupService.class);
  private final PasswordEncoder passwordEncoder =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();
  private final AccountService accountService =
      new AccountService(appUserRepository, staffLookupService);
  private final AuthService authService =
      new AuthService(
          appUserRepository, accountService, passwordEncoder, TestTokens.issuer(Clock.systemUTC()));

  @Test
  void signsInWithTheRightPasswordAndAddsTheStaffId() {
    AppUser maya = user("maya.chen@example.com", UserRole.STAFF);
    when(appUserRepository.findByEmail("maya.chen@example.com")).thenReturn(Optional.of(maya));
    when(staffLookupService.findStaffIdByUserId(5L)).thenReturn(Optional.of(2L));

    AuthResponse response =
        authService.login(new LoginRequest(" Maya.Chen@example.com ", PASSWORD));

    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.account().staffId()).isEqualTo(2L);
    assertThat(TestTokens.decoder().decode(response.accessToken()).getSubject()).isEqualTo("5");
  }

  @Test
  void rejectsAWrongPassword() {
    when(appUserRepository.findByEmail("maya.chen@example.com"))
        .thenReturn(Optional.of(user("maya.chen@example.com", UserRole.STAFF)));

    assertThatThrownBy(
            () -> authService.login(new LoginRequest("maya.chen@example.com", "wrong-password")))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid e-mail or password.");
  }

  @Test
  void rejectsAnUnknownEmailAfterCheckingAPasswordAnyway() {
    PasswordEncoder spyEncoder = mock(PasswordEncoder.class);
    AuthService service =
        new AuthService(
            appUserRepository, accountService, spyEncoder, TestTokens.issuer(Clock.systemUTC()));
    when(appUserRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login(new LoginRequest("nobody@example.com", PASSWORD)))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid e-mail or password.");
    verify(spyEncoder).matches(anyString(), anyString());
  }

  private AppUser user(String email, UserRole role) {
    AppUser user = new AppUser("Maya Chen", email, passwordEncoder.encode(PASSWORD), role);
    ReflectionTestUtils.setField(user, "userId", 5L);
    user.setOrganizationId(1L);
    return user;
  }
}
