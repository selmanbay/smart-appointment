package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.catalog.CatalogService;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation changeAppointmentStatus (UC-8, SDD Sequence 3). One transaction records the
 * status, its server time and, on completion, the service's new learned duration (Eq. 3), so the
 * next waiting estimate already uses it.
 */
@Service
public class AppointmentStatusService {

  private final AppointmentChangeLoader appointmentChangeLoader;
  private final CatalogService catalogService;
  private final AppointmentResponseMapper appointmentResponseMapper;
  private final Clock clock;

  AppointmentStatusService(
      AppointmentChangeLoader appointmentChangeLoader,
      CatalogService catalogService,
      AppointmentResponseMapper appointmentResponseMapper,
      Clock clock) {
    this.appointmentChangeLoader = appointmentChangeLoader;
    this.catalogService = catalogService;
    this.appointmentResponseMapper = appointmentResponseMapper;
    this.clock = clock;
  }

  /**
   * Only the appointment's staff member or the organization's owner (403 otherwise); a change SRS
   * 2.5 does not allow answers 409 INVALID_STATUS and changes nothing.
   */
  @Transactional
  public AppointmentResponse changeStatus(
      Long appointmentId, Long callerUserId, AppointmentStatus newStatus) {
    AppointmentChange change = appointmentChangeLoader.load(appointmentId, callerUserId);
    change.requireStaffOrOwner();
    Appointment appointment = change.appointment();
    StatusTransition transition =
        StatusTransition.find(appointment.getStatus(), newStatus)
            .orElseThrow(BookingProblem.INVALID_STATUS::exception);
    transition.apply(appointment, clock.instant());
    if (transition.completesService()) {
      catalogService.recordServiceDuration(
          appointment.getServiceId(), appointment.measuredServiceDuration());
    }
    return appointmentResponseMapper.toResponse(
        appointment, change.context(), null, change.caller());
  }
}
