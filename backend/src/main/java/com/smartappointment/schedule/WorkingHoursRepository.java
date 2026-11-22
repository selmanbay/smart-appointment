package com.smartappointment.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link WorkingHours} (SRS Table 9). */
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

  List<WorkingHours> findByStaffIdOrderByDayOfWeek(Long staffId);
}
