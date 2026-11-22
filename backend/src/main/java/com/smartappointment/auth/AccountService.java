package com.smartappointment.auth;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds the account view (contract: Account) from a user and their staff record. */
@Service
public class AccountService {

  private final AppUserRepository appUserRepository;
  private final StaffLookupService staffLookupService;

  public AccountService(
      AppUserRepository appUserRepository, StaffLookupService staffLookupService) {
    this.appUserRepository = appUserRepository;
    this.staffLookupService = staffLookupService;
  }

  @Transactional(readOnly = true)
  public AccountResponse currentAccount(Long userId) {
    return appUserRepository
        .findById(userId)
        .map(this::toAccount)
        .orElseThrow(() -> new ResourceNotFoundException("Account", userId));
  }

  AccountResponse toAccount(AppUser user) {
    Long staffId = staffLookupService.findStaffIdByUserId(user.getUserId()).orElse(null);
    return new AccountResponse(
        user.getUserId(),
        user.getFullName(),
        user.getEmail(),
        user.getPhone(),
        user.getRole(),
        user.getOrganizationId(),
        staffId);
  }
}
