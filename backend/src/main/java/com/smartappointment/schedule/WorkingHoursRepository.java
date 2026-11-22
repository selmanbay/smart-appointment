package com.smartappointment.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link WorkingHours} (SRS Table 9). */
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {}
