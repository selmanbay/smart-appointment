package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.ISTANBUL;
import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.STAFF_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.auth.AccountService;
import com.smartappointment.auth.UserRole;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AgendaResponse;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.common.web.ForbiddenException;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.organization.OrganizationService;
import com.smartappointment.schedule.dto.WorkingHoursResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** UC-8: who sees a staff member's agenda. */
@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

  private static final AccountResponse MAYA =
      new AccountResponse(4L, "Maya Chen", "m@example.com", null, UserRole.STAFF, 1L, STAFF_ID);
  private static final AccountResponse DANIEL =
      new AccountResponse(5L, "Daniel Brooks", "d@example.com", null, UserRole.STAFF, 1L, 2L);
  private static final AccountResponse OWNER =
      new AccountResponse(
          2L, "Hannah Reid", "h@example.com", null, UserRole.ORGANIZATION_OWNER, 1L, null);
  private static final WorkingHoursResponse MONDAY_HOURS =
      new WorkingHoursResponse((short) 1, "09:00", "16:00", "12:00", "13:00");

  @Mock private StaffLookupService staffLookupService;
  @Mock private OrganizationService organizationService;
  @Mock private AgendaAppointments agendaAppointments;
  @Mock private AccountService accountService;

  @Test
  void aStaffMemberSeesTheirOwnDayWithItsWorkingHours() {
    givenStaffOfOrganization(1L);
    when(accountService.currentAccount(MAYA.userId())).thenReturn(MAYA);
    when(organizationService.getTimezone(1L)).thenReturn(ISTANBUL);
    when(agendaAppointments.forDay(STAFF_ID, MONDAY, ISTANBUL)).thenReturn(List.of());
    when(agendaAppointments.workingHours(STAFF_ID, MONDAY)).thenReturn(MONDAY_HOURS);

    AgendaResponse agenda = service().getAgenda(STAFF_ID, MONDAY, MAYA.userId());

    assertThat(agenda.date()).isEqualTo(MONDAY);
    assertThat(agenda.staffId()).isEqualTo(STAFF_ID);
    assertThat(agenda.workingHours()).isEqualTo(MONDAY_HOURS);
    assertThat(agenda.appointments()).isEmpty();
  }

  @Test
  void theOwnerSeesAnyStaffAgendaOfTheOrganization() {
    givenStaffOfOrganization(1L);
    when(accountService.currentAccount(OWNER.userId())).thenReturn(OWNER);
    when(organizationService.getTimezone(1L)).thenReturn(ISTANBUL);

    assertThat(service().getAgenda(STAFF_ID, MONDAY, OWNER.userId()).workingHours()).isNull();
  }

  @Test
  void anotherStaffMemberIsForbidden() {
    givenStaffOfOrganization(1L);
    when(accountService.currentAccount(DANIEL.userId())).thenReturn(DANIEL);

    assertThatThrownBy(() -> service().getAgenda(STAFF_ID, MONDAY, DANIEL.userId()))
        .isInstanceOf(ForbiddenException.class);
    verifyNoInteractions(agendaAppointments);
  }

  @Test
  void aMissingStaffMemberIsNotFound() {
    when(staffLookupService.findOrganizationId(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().getAgenda(99L, MONDAY, MAYA.userId()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  private void givenStaffOfOrganization(long organizationId) {
    when(staffLookupService.findOrganizationId(STAFF_ID)).thenReturn(Optional.of(organizationId));
  }

  private AgendaService service() {
    return new AgendaService(
        staffLookupService, organizationService, agendaAppointments, accountService);
  }
}
