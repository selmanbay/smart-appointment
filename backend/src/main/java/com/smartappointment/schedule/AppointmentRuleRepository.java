package com.smartappointment.schedule;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link AppointmentRule} (SRS Table 9). */
public interface AppointmentRuleRepository extends JpaRepository<AppointmentRule, Long> {

  Optional<AppointmentRule> findByServiceId(Long serviceId);
}
