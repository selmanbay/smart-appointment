package com.smartappointment.schedule;

import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * {@link WorkingHours} in the contract's shape: times as {@code HH:mm} (contract: WorkingHours).
 */
final class WorkingHoursMapping {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private WorkingHoursMapping() {}

  static WorkingHoursResponse toResponse(WorkingHours workingHours) {
    return new WorkingHoursResponse(
        workingHours.getDayOfWeek(),
        format(workingHours.getStartTime()),
        format(workingHours.getEndTime()),
        format(workingHours.getBreakStart()),
        format(workingHours.getBreakEnd()));
  }

  private static String format(LocalTime time) {
    return time == null ? null : time.format(TIME_FORMAT);
  }
}
