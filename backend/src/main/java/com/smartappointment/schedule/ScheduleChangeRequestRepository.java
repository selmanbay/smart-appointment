package com.smartappointment.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link ScheduleChangeRequest} (SRS Table 9). */
public interface ScheduleChangeRequestRepository
    extends JpaRepository<ScheduleChangeRequest, Long> {}
