package com.smartappointment.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.ResourceNotFoundException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

  @Mock private AppointmentRuleRepository appointmentRuleRepository;
  @Mock private WorkingHoursRepository workingHoursRepository;
  @Mock private StaffLookupService staffLookupService;

  @Test
  void getAppointmentRuleMapsEveryLimit() {
    AppointmentRule rule = new AppointmentRule(1L);
    rule.setMaxAdvanceDays(30);
    rule.setMinLeadHours(2);
    when(appointmentRuleRepository.findByServiceId(1L)).thenReturn(Optional.of(rule));

    var response = service().getAppointmentRule(1L);

    assertThat(response.maxAdvanceDays()).isEqualTo(30);
    assertThat(response.minLeadHours()).isEqualTo(2);
  }

  @Test
  void getAppointmentRuleThrowsWhenTheServiceHasNoRule() {
    when(appointmentRuleRepository.findByServiceId(2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().getAppointmentRule(2L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getWorkingHoursReturnsEveryConfiguredDayInOrder() {
    when(staffLookupService.existsById(1L)).thenReturn(true);
    WorkingHours monday = new WorkingHours(1L, (short) 1, LocalTime.of(9, 0), LocalTime.of(16, 0));
    when(workingHoursRepository.findByStaffIdOrderByDayOfWeek(1L)).thenReturn(List.of(monday));

    List<?> hours = service().getWorkingHours(1L);

    assertThat(hours).hasSize(1);
  }

  @Test
  void getWorkingHoursThrowsWhenTheStaffMemberIsMissing() {
    when(staffLookupService.existsById(404L)).thenReturn(false);

    assertThatThrownBy(() -> service().getWorkingHours(404L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  private ScheduleService service() {
    return new ScheduleService(
        new ScheduleLookupService(appointmentRuleRepository, workingHoursRepository),
        workingHoursRepository,
        staffLookupService);
  }
}
