package com.smartappointment.catalog;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence for {@link Service} (SRS Table 9). */
public interface ServiceRepository extends JpaRepository<Service, Long> {

  List<Service> findByOrganizationIdAndIsActiveTrue(Long organizationId);

  /** SELECT ... FOR UPDATE: two completions of the same service update Eq. 3 one after another. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Service s where s.serviceId = :serviceId")
  Optional<Service> findLockedById(@Param("serviceId") Long serviceId);
}
