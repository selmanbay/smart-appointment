package com.smartappointment.booking;

import com.smartappointment.catalog.CatalogService;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.catalog.dto.ServiceResponse;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.OrganizationService;
import com.smartappointment.schedule.ScheduleLookupService;
import com.smartappointment.schedule.dto.AppointmentRuleResponse;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** Collects a {@link BookingContext} from the catalog, organization and schedule features. */
@Component
class BookingContextLoader {

  private static final AppointmentRuleResponse NO_RULE =
      new AppointmentRuleResponse(null, null, null, null);

  private final CatalogService catalogService;
  private final StaffLookupService staffLookupService;
  private final OrganizationService organizationService;
  private final ScheduleLookupService scheduleLookupService;

  BookingContextLoader(
      CatalogService catalogService,
      StaffLookupService staffLookupService,
      OrganizationService organizationService,
      ScheduleLookupService scheduleLookupService) {
    this.catalogService = catalogService;
    this.staffLookupService = staffLookupService;
    this.organizationService = organizationService;
    this.scheduleLookupService = scheduleLookupService;
  }

  /**
   * For reads: 404 when the service is missing or inactive, or the staff member does not offer it.
   */
  BookingContext load(Long staffId, Long serviceId, LocalDate date) {
    ServiceResponse service = catalogService.getActiveService(serviceId);
    int capacity =
        staffLookupService
            .findCapacityOfActiveProvider(staffId, serviceId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Active staff member providing service " + serviceId + ", id", staffId));
    return forDay(staffId, service, capacity, date);
  }

  /**
   * For a booking that starts at {@code start}: 404 for a missing or inactive service, 409
   * STAFF_DOES_NOT_PROVIDE_SERVICE when the staff member is inactive or does not offer it (SDD
   * Activity 1, first check).
   */
  BookingContext loadForBooking(Long staffId, Long serviceId, Instant start) {
    ServiceResponse service = catalogService.getActiveService(serviceId);
    int capacity =
        staffLookupService
            .findCapacityOfActiveProvider(staffId, serviceId)
            .orElseThrow(BookingProblem.STAFF_DOES_NOT_PROVIDE_SERVICE::exception);
    return forStart(staffId, service, capacity, start);
  }

  /**
   * For an appointment that already exists (cancel, reschedule, replay): its service and staff
   * member as they are now, even if inactive since; 404 only if they no longer exist.
   */
  BookingContext loadForAppointment(Appointment appointment) {
    ServiceResponse service = catalogService.getService(appointment.getServiceId());
    int capacity =
        staffLookupService
            .findCapacity(appointment.getStaffId())
            .orElseThrow(() -> new ResourceNotFoundException("Staff", appointment.getStaffId()));
    return forStart(appointment.getStaffId(), service, capacity, appointment.getStartTime());
  }

  private BookingContext forStart(
      Long staffId, ServiceResponse service, int capacity, Instant start) {
    LocalDate date =
        LocalDate.ofInstant(start, organizationService.getTimezone(service.organizationId()));
    return forDay(staffId, service, capacity, date);
  }

  private BookingContext forDay(
      Long staffId, ServiceResponse service, int capacity, LocalDate date) {
    return new BookingContext(
        staffId,
        service,
        capacity,
        organizationService.getTimezone(service.organizationId()),
        scheduleLookupService.findWorkingPeriods(staffId, date.getDayOfWeek()),
        scheduleLookupService.findAppointmentRule(service.serviceId()).orElse(NO_RULE));
  }
}
