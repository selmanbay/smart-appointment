package com.smartappointment.schedule;

import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Contract operations getAppointmentRule and getWorkingHours. */
@RestController
public class ScheduleController {

  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping("/api/v1/services/{serviceId}/rule")
  public AppointmentRuleResponse getAppointmentRule(@PathVariable Long serviceId) {
    return scheduleService.getAppointmentRule(serviceId);
  }

  @GetMapping("/api/v1/staff/{staffId}/working-hours")
  public List<WorkingHoursResponse> getWorkingHours(@PathVariable Long staffId) {
    return scheduleService.getWorkingHours(staffId);
  }
}
