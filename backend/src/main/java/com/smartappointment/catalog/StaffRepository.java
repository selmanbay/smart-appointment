package com.smartappointment.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Staff} (SRS Table 9). */
public interface StaffRepository extends JpaRepository<Staff, Long> {

  Optional<Staff> findByUserId(Long userId);
}
