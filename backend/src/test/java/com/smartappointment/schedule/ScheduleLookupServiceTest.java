package com.smartappointment.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.smartappointment.schedule.dto.WorkingPeriod;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleLookupServiceTest {

  private static final short MONDAY = 1;

  @Mock private AppointmentRuleRepository appointmentRuleRepository;
  @Mock private WorkingHoursRepository workingHoursRepository;

  @Test
  void theBreakSplitsTheWorkingHoursIntoTwoPeriods() {
    WorkingHours hours = new WorkingHours(1L, MONDAY, LocalTime.of(9, 0), LocalTime.of(16, 0));
    hours.setBreakStart(LocalTime.of(12, 0));
    hours.setBreakEnd(LocalTime.of(13, 0));
    when(workingHoursRepository.findByStaffIdAndDayOfWeekOrderByStartTime(1L, MONDAY))
        .thenReturn(List.of(hours));

    assertThat(lookup().findWorkingPeriods(1L, DayOfWeek.MONDAY))
        .containsExactly(
            new WorkingPeriod(LocalTime.of(9, 0), LocalTime.of(12, 0)),
            new WorkingPeriod(LocalTime.of(13, 0), LocalTime.of(16, 0)));
  }

  @Test
  void workingHoursWithoutABreakAreOnePeriod() {
    WorkingHours hours = new WorkingHours(1L, MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
    when(workingHoursRepository.findByStaffIdAndDayOfWeekOrderByStartTime(1L, MONDAY))
        .thenReturn(List.of(hours));

    assertThat(lookup().findWorkingPeriods(1L, DayOfWeek.MONDAY))
        .containsExactly(new WorkingPeriod(LocalTime.of(9, 0), LocalTime.of(12, 0)));
  }

  @Test
  void aServiceWithoutARuleHasNoLimits() {
    when(appointmentRuleRepository.findByServiceId(2L)).thenReturn(Optional.empty());

    assertThat(lookup().findAppointmentRule(2L)).isEmpty();
  }

  private ScheduleLookupService lookup() {
    return new ScheduleLookupService(appointmentRuleRepository, workingHoursRepository);
  }
}
