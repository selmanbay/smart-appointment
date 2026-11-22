package com.smartappointment.schedule;

import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contract operations getAppointmentRule and getWorkingHours. */
@Service
public class ScheduleService {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final AppointmentRuleRepository appointmentRuleRepository;
  private final WorkingHoursRepository workingHoursRepository;
  private final StaffLookupService staffLookupService;

  public ScheduleService(
      AppointmentRuleRepository appointmentRuleRepository,
      WorkingHoursRepository workingHoursRepository,
      StaffLookupService staffLookupService) {
    this.appointmentRuleRepository = appointmentRuleRepository;
    this.workingHoursRepository = workingHoursRepository;
    this.staffLookupService = staffLookupService;
  }

  @Transactional(readOnly = true)
  public AppointmentRuleResponse getAppointmentRule(Long serviceId) {
    AppointmentRule rule =
        appointmentRuleRepository
            .findByServiceId(serviceId)
            .orElseThrow(
                () -> new ResourceNotFoundException("AppointmentRule for service", serviceId));
    return new AppointmentRuleResponse(
        rule.getMaxAdvanceDays(),
        rule.getMinLeadHours(),
        rule.getCancelDeadlineHours(),
        rule.getMaxActivePerUser());
  }

  @Transactional(readOnly = true)
  public List<WorkingHoursResponse> getWorkingHours(Long staffId) {
    if (!staffLookupService.existsById(staffId)) {
      throw new ResourceNotFoundException("Staff", staffId);
    }
    return workingHoursRepository.findByStaffIdOrderByDayOfWeek(staffId).stream()
        .map(ScheduleService::toResponse)
        .toList();
  }

  private static WorkingHoursResponse toResponse(WorkingHours workingHours) {
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
