package com.smartappointment.schedule;

import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contract operations getAppointmentRule and getWorkingHours. */
@Service
public class ScheduleService {

  private final ScheduleLookupService scheduleLookupService;
  private final WorkingHoursRepository workingHoursRepository;
  private final StaffLookupService staffLookupService;

  public ScheduleService(
      ScheduleLookupService scheduleLookupService,
      WorkingHoursRepository workingHoursRepository,
      StaffLookupService staffLookupService) {
    this.scheduleLookupService = scheduleLookupService;
    this.workingHoursRepository = workingHoursRepository;
    this.staffLookupService = staffLookupService;
  }

  @Transactional(readOnly = true)
  public AppointmentRuleResponse getAppointmentRule(Long serviceId) {
    return scheduleLookupService
        .findAppointmentRule(serviceId)
        .orElseThrow(() -> new ResourceNotFoundException("AppointmentRule for service", serviceId));
  }

  @Transactional(readOnly = true)
  public List<WorkingHoursResponse> getWorkingHours(Long staffId) {
    if (!staffLookupService.existsById(staffId)) {
      throw new ResourceNotFoundException("Staff", staffId);
    }
    return workingHoursRepository.findByStaffIdOrderByDayOfWeek(staffId).stream()
        .map(WorkingHoursMapping::toResponse)
        .toList();
  }
}
