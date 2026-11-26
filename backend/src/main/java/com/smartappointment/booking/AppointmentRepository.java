package com.smartappointment.booking;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence for {@link Appointment} (SRS Table 9). */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  /** A staff member's appointments in the given statuses that overlap [from, until). */
  @Query(
      "select a from Appointment a where a.staffId = :staffId and a.status in :statuses"
          + " and a.startTime < :until and a.endTime > :from order by a.startTime")
  List<Appointment> findOverlapping(
      @Param("staffId") Long staffId,
      @Param("statuses") Collection<AppointmentStatus> statuses,
      @Param("from") Instant from,
      @Param("until") Instant until);

  /** SELECT ... FOR UPDATE: a cancel and a reschedule of the same appointment run one at a time. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Appointment a where a.appointmentId = :appointmentId")
  Optional<Appointment> findLockedById(@Param("appointmentId") Long appointmentId);

  long countByCustomerIdAndServiceIdAndStatusIn(
      Long customerId, Long serviceId, Collection<AppointmentStatus> statuses);

  /** UC-6 upcoming: still active and not yet ended, nearest first. */
  @Query(
      "select a from Appointment a where a.customerId = :customerId and a.status in :active"
          + " and a.endTime > :now order by a.startTime asc, a.appointmentId asc")
  Page<Appointment> findUpcoming(
      @Param("customerId") Long customerId,
      @Param("active") Collection<AppointmentStatus> active,
      @Param("now") Instant now,
      Pageable pageable);

  /** UC-6 past: everything that is not upcoming, newest first. */
  @Query(
      "select a from Appointment a where a.customerId = :customerId"
          + " and (a.status not in :active or a.endTime <= :now)"
          + " order by a.startTime desc, a.appointmentId desc")
  Page<Appointment> findPast(
      @Param("customerId") Long customerId,
      @Param("active") Collection<AppointmentStatus> active,
      @Param("now") Instant now,
      Pageable pageable);
}
