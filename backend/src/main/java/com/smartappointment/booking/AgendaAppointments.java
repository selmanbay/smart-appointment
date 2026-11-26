package com.smartappointment.booking;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.schedule.ScheduleLookupService;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;

/** What an agenda shows: the day's appointments and working hours of one staff member. */
@Component
class AgendaAppointments {

  private final AppointmentRepository appointmentRepository;
  private final ScheduleLookupService scheduleLookupService;
  private final AppointmentViews appointmentViews;

  AgendaAppointments(
      AppointmentRepository appointmentRepository,
      ScheduleLookupService scheduleLookupService,
      AppointmentViews appointmentViews) {
    this.appointmentRepository = appointmentRepository;
    this.scheduleLookupService = scheduleLookupService;
    this.appointmentViews = appointmentViews;
  }

  /** Midnight to midnight in the organization's time zone, cancelled ones left out. */
  List<Appointment> forDay(Long staffId, LocalDate date, ZoneId zone) {
    Instant dayStart = date.atStartOfDay(zone).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
    return appointmentRepository.findOverlapping(
        staffId, AppointmentStatus.NOT_CANCELLED, dayStart, dayEnd);
  }

  AppointmentResponse view(Appointment appointment, AccountResponse viewer) {
    return appointmentViews.view(appointment, viewer);
  }

  /** Null on a day off (contract: Agenda.workingHours). */
  WorkingHoursResponse workingHours(Long staffId, LocalDate date) {
    return scheduleLookupService.findWorkingHours(staffId, date.getDayOfWeek()).orElse(null);
  }
}
