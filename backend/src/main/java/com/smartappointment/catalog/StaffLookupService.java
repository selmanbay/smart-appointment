package com.smartappointment.catalog;

import com.smartappointment.catalog.dto.StaffMemberSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

  /**
   * The capacity of an active staff member who provides the service (SRS 2.5); empty when the staff
   * member is missing, inactive or does not provide it.
   */
  @Transactional(readOnly = true)
  public Optional<Integer> findCapacityOfActiveProvider(Long staffId, Long serviceId) {
    return staffRepository
        .findByStaffIdAndIsActiveTrueAndServicesServiceId(staffId, serviceId)
        .map(Staff::getCapacity);
  }

  /** The capacity of a staff member, active or not; empty when the staff member does not exist. */
  @Transactional(readOnly = true)
  public Optional<Integer> findCapacity(Long staffId) {
    return staffRepository.findById(staffId).map(Staff::getCapacity);
  }

  /** Active staff members who provide the service (SRS 2.5), in staff id order. */
  @Transactional(readOnly = true)
  public List<StaffMemberSummary> findActiveProviders(Long serviceId) {
    return staffRepository.findActiveProviders(serviceId);
  }

  /** Name and title of a staff member, active or not. */
  @Transactional(readOnly = true)
  public Optional<StaffMemberSummary> findMemberSummary(Long staffId) {
    return staffRepository.findMemberSummary(staffId);
  }

  /** The organization the staff member works for; empty when the staff member does not exist. */
  @Transactional(readOnly = true)
  public Optional<Long> findOrganizationId(Long staffId) {
    return staffRepository.findOrganizationId(staffId);
  }

  /**
   * Locks the staff member's row until the caller's transaction ends, so concurrent bookings of the
   * same staff member count capacity one after another (ADR-0018). Needs a running transaction.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void lockStaffMember(Long staffId) {
    staffRepository.lockById(staffId);
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long staffId) {
    return staffRepository.existsById(staffId);
  }
}
