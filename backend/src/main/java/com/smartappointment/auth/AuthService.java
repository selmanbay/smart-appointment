package com.smartappointment.auth;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.auth.dto.AuthResponse;
import com.smartappointment.auth.dto.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sign-in with e-mail and password (F1, UC-2, ADR-0015). */
@Service
public class AuthService {

  /**
   * Compared against when the e-mail is unknown, so a wrong e-mail takes as long as a wrong
   * password and does not reveal which accounts exist.
   */
  private static final String UNKNOWN_ACCOUNT_HASH =
      "{bcrypt}$2a$10$odiChae1pJ4SbbV6c6CMTOIi3oi8y6AOYql6dUsj3GojeMPPrTWqa";

  private static final String INVALID_CREDENTIALS = "Invalid e-mail or password.";

  private final AppUserRepository appUserRepository;
  private final AccountService accountService;
  private final PasswordEncoder passwordEncoder;
  private final AccessTokenService accessTokenService;

  public AuthService(
      AppUserRepository appUserRepository,
      AccountService accountService,
      PasswordEncoder passwordEncoder,
      AccessTokenService accessTokenService) {
    this.appUserRepository = appUserRepository;
    this.accountService = accountService;
    this.passwordEncoder = passwordEncoder;
    this.accessTokenService = accessTokenService;
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    AppUser user = appUserRepository.findByEmail(request.normalizedEmail()).orElse(null);
    String storedHash = user == null ? UNKNOWN_ACCOUNT_HASH : user.getPasswordHash();
    boolean isPasswordCorrect = passwordEncoder.matches(request.password(), storedHash);
    if (user == null || !isPasswordCorrect) {
      throw new BadCredentialsException(INVALID_CREDENTIALS);
    }
    AccountResponse account = accountService.toAccount(user);
    AccessToken token = accessTokenService.issue(account);
    return new AuthResponse(
        token.value(), AuthResponse.BEARER, token.lifetime().toSeconds(), account);
  }
}
