package com.smartappointment.booking;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.smartappointment.catalog.CatalogService;
import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.estimation.LearnedDuration;
import com.smartappointment.estimation.ScheduledService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Loads a {@link BookedDay}: the bookings and the learned durations of their services. */
@Component
class BookedDayLoader {

  private final AppointmentRepository appointmentRepository;
  private final CatalogService catalogService;

  BookedDayLoader(AppointmentRepository appointmentRepository, CatalogService catalogService) {
    this.appointmentRepository = appointmentRepository;
    this.catalogService = catalogService;
  }

  /** The day runs from midnight to midnight in the organization's time zone. */
  BookedDay load(Long staffId, LocalDate date, ZoneId zone) {
    Instant dayStart = date.atStartOfDay(zone).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
    List<Appointment> booked =
        appointmentRepository.findOverlapping(
            staffId, AppointmentStatus.NOT_CANCELLED, dayStart, dayEnd);
    List<Appointment> holdingCapacity =
        booked.stream()
            .filter(
                appointment -> AppointmentStatus.HOLDING_CAPACITY.contains(appointment.getStatus()))
            .toList();
    Map<Long, Duration> durationByServiceId = learnedDurations(holdingCapacity);
    List<ScheduledService> queue =
        holdingCapacity.stream()
            .map(appointment ->
                new ScheduledService(
                    appointment.getStartTime(),
                    durationByServiceId.get(appointment.getServiceId())))
            .toList();
    return new BookedDay(holdingCapacity, queue, booked.size());
  }

  private Map<Long, Duration> learnedDurations(List<Appointment> appointments) {
    if (appointments.isEmpty()) {
      return Map.of();
    }
    return catalogService
        .findServices(appointments.stream().map(Appointment::getServiceId).collect(toSet()))
        .stream()
        .collect(toMap(ServiceResponse::serviceId, BookedDayLoader::learnedDuration));
  }

  private static Duration learnedDuration(ServiceResponse service) {
    return LearnedDuration.current(
        service.learnedDurationMinutes(), service.expectedDurationMinutes());
  }
}
