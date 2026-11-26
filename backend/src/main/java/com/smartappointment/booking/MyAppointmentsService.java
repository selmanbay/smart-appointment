package com.smartappointment.booking;

import com.smartappointment.auth.AccountService;
import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.booking.dto.AppointmentPageResponse;
import com.smartappointment.booking.dto.AppointmentScope;
import com.smartappointment.common.web.PageMetadata;
import java.time.Clock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contract operation listMyAppointments (UC-6). */
@Service
public class MyAppointmentsService {

  private final AppointmentRepository appointmentRepository;
  private final AccountService accountService;
  private final AppointmentViews appointmentViews;
  private final Clock clock;

  MyAppointmentsService(
      AppointmentRepository appointmentRepository,
      AccountService accountService,
      AppointmentViews appointmentViews,
      Clock clock) {
    this.appointmentRepository = appointmentRepository;
    this.accountService = accountService;
    this.appointmentViews = appointmentViews;
    this.clock = clock;
  }

  /**
   * The signed-in customer's appointments: UPCOMING are still active and not yet ended, nearest
   * first; PAST are all others, newest first.
   */
  @Transactional(readOnly = true)
  public AppointmentPageResponse listMyAppointments(
      Long customerId, AppointmentScope scope, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<Appointment> appointments =
        scope == AppointmentScope.UPCOMING
            ? appointmentRepository.findUpcoming(
                customerId, AppointmentStatus.HOLDING_CAPACITY, clock.instant(), pageRequest)
            : appointmentRepository.findPast(
                customerId, AppointmentStatus.HOLDING_CAPACITY, clock.instant(), pageRequest);
    AccountResponse customer = accountService.currentAccount(customerId);
    return new AppointmentPageResponse(
        appointments.getContent().stream()
            .map(appointment -> appointmentViews.view(appointment, customer))
            .toList(),
        PageMetadata.from(appointments));
  }
}
