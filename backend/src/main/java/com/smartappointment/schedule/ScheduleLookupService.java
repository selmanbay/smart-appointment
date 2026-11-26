package com.smartappointment.schedule;

import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import com.smartappointment.schedule.dto.WorkingPeriod;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lets other features read rules and working periods without touching schedule repositories. */
@Service
public class ScheduleLookupService {

  private final AppointmentRuleRepository appointmentRuleRepository;
  private final WorkingHoursRepository workingHoursRepository;

  public ScheduleLookupService(
      AppointmentRuleRepository appointmentRuleRepository,
      WorkingHoursRepository workingHoursRepository) {
    this.appointmentRuleRepository = appointmentRuleRepository;
    this.workingHoursRepository = workingHoursRepository;
  }

  /** Empty when the service has no rule; its booking limits then do not apply (SRS 2.5). */
  @Transactional(readOnly = true)
  public Optional<AppointmentRuleResponse> findAppointmentRule(Long serviceId) {
    return appointmentRuleRepository
        .findByServiceId(serviceId)
        .map(rule ->
            new AppointmentRuleResponse(
                rule.getMaxAdvanceDays(),
                rule.getMinLeadHours(),
                rule.getCancelDeadlineHours(),
                rule.getMaxActivePerUser()));
  }

  /** The staff member's working hours on that weekday, split by the break (SRS Eq. 1, L_j). */
  @Transactional(readOnly = true)
  public List<WorkingPeriod> findWorkingPeriods(Long staffId, DayOfWeek dayOfWeek) {
    return workingHoursRepository
        .findByStaffIdAndDayOfWeekOrderByStartTime(staffId, (short) dayOfWeek.getValue())
        .stream()
        .flatMap(workingHours -> splitByBreak(workingHours).stream())
        .toList();
  }

  /** The working hours on that weekday for the agenda (UC-8); empty on a day off. */
  @Transactional(readOnly = true)
  public Optional<WorkingHoursResponse> findWorkingHours(Long staffId, DayOfWeek dayOfWeek) {
    return workingHoursRepository
        .findByStaffIdAndDayOfWeekOrderByStartTime(staffId, (short) dayOfWeek.getValue())
        .stream()
        .findFirst()
        .map(WorkingHoursMapping::toResponse);
  }

  private static List<WorkingPeriod> splitByBreak(WorkingHours workingHours) {
    if (workingHours.getBreakStart() == null) {
      return List.of(new WorkingPeriod(workingHours.getStartTime(), workingHours.getEndTime()));
    }
    return List.of(
        new WorkingPeriod(workingHours.getStartTime(), workingHours.getBreakStart()),
        new WorkingPeriod(workingHours.getBreakEnd(), workingHours.getEndTime()));
  }
}
