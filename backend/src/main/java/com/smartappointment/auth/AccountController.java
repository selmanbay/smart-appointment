package com.smartappointment.auth;

import com.smartappointment.auth.dto.AccountResponse;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation getCurrentAccount. The principal name is the token subject (user id). */
@RestController
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping("/api/v1/me")
  public AccountResponse currentAccount(Principal principal) {
    return accountService.currentAccount(Long.valueOf(principal.getName()));
  }
}
