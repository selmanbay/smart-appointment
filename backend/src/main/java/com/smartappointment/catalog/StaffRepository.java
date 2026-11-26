package com.smartappointment.catalog;

import com.smartappointment.catalog.dto.StaffMemberSummary;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence for {@link Staff} (SRS Table 9). */
public interface StaffRepository extends JpaRepository<Staff, Long> {

  Optional<Staff> findByUserId(Long userId);

  Optional<Staff> findByStaffIdAndIsActiveTrueAndServicesServiceId(Long staffId, Long serviceId);

  /**
   * The name lives on the user account; the join is by entity name so catalog does not depend on
   * the auth package, which already depends on catalog.
   */
  @Query(
      "select new com.smartappointment.catalog.dto.StaffMemberSummary("
          + "s.staffId, u.fullName, s.title, s.capacity)"
          + " from Staff s join s.services provided join AppUser u on u.userId = s.userId"
          + " where provided.serviceId = :serviceId and s.isActive = true order by s.staffId")
  List<StaffMemberSummary> findActiveProviders(@Param("serviceId") Long serviceId);

  @Query(
      "select new com.smartappointment.catalog.dto.StaffMemberSummary("
          + "s.staffId, u.fullName, s.title, s.capacity)"
          + " from Staff s join AppUser u on u.userId = s.userId where s.staffId = :staffId")
  Optional<StaffMemberSummary> findMemberSummary(@Param("staffId") Long staffId);

  /** The staff member's organization lives on the user account (joined by entity name). */
  @Query(
      "select u.organizationId from Staff s join AppUser u on u.userId = s.userId"
          + " where s.staffId = :staffId")
  Optional<Long> findOrganizationId(@Param("staffId") Long staffId);

  /** SELECT ... FOR UPDATE on the staff row: serializes bookings of one staff member (ADR-0018). */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Staff s where s.staffId = :staffId")
  Optional<Staff> lockById(@Param("staffId") Long staffId);
}
