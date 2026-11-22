package com.smartappointment.catalog;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lets other features find a user's staff record without touching the catalog repository. */
@Service
public class StaffLookupService {

  private final StaffRepository staffRepository;

  public StaffLookupService(StaffRepository staffRepository) {
    this.staffRepository = staffRepository;
  }

  @Transactional(readOnly = true)
  public Optional<Long> findStaffIdByUserId(Long userId) {
    return staffRepository.findByUserId(userId).map(Staff::getStaffId);
  }
}
