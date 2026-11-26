package com.smartappointment.booking;

import com.smartappointment.auth.AccountService;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AgendaResponse;
import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.ForbiddenException;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.OrganizationService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation getStaffAgenda (UC-8, SDD Sequence 3): one staff member's day in time order,
 * with that day's working hours. Cancelled appointments are left out; no-shows and completed ones
 * stay, so the day's record is complete.
 */
@Service
public class AgendaService {

  private final StaffLookupService staffLookupService;
  private final OrganizationService organizationService;
  private final AgendaAppointments agendaAppointments;
  private final AccountService accountService;

  AgendaService(
      StaffLookupService staffLookupService,
      OrganizationService organizationService,
      AgendaAppointments agendaAppointments,
      AccountService accountService) {
    this.staffLookupService = staffLookupService;
    this.organizationService = organizationService;
    this.agendaAppointments = agendaAppointments;
    this.accountService = accountService;
  }

  /** 404 for a missing staff member; 403 unless the viewer is that staff member or the owner. */
  @Transactional(readOnly = true)
  public AgendaResponse getAgenda(Long staffId, LocalDate date, Long viewerUserId) {
    Long organizationId =
        staffLookupService
            .findOrganizationId(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));
    AccountResponse viewer = accountService.currentAccount(viewerUserId);
    boolean isOwnAgenda = staffId.equals(viewer.staffId());
    if (!isOwnAgenda && !AccessRules.isOrganizationOwner(viewer, organizationId)) {
      throw new ForbiddenException("Only the staff member or the owner can see this agenda.");
    }
    ZoneId zone = organizationService.getTimezone(organizationId);
    List<AppointmentResponse> appointments =
        agendaAppointments.forDay(staffId, date, zone).stream()
            .map(appointment -> agendaAppointments.view(appointment, viewer))
            .toList();
    return new AgendaResponse(
        date, staffId, agendaAppointments.workingHours(staffId, date), appointments);
  }
}
