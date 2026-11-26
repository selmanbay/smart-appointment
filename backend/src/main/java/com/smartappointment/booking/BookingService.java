package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.BookingRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract operation bookAppointment (UC-3 step 6, SDD Activity 1). Every check and the insert run
 * in one transaction; the capacity count runs under a lock on the staff row (ADR-0018), so
 * concurrent requests for the last place cannot both succeed.
 */
@Service
public class BookingService {

  private final BookingContextLoader bookingContextLoader;
  private final BookingRuleChecker bookingRuleChecker;
  private final BookingRecorder bookingRecorder;
  private final AppointmentResponseMapper appointmentResponseMapper;

  BookingService(
      BookingContextLoader bookingContextLoader,
      BookingRuleChecker bookingRuleChecker,
      BookingRecorder bookingRecorder,
      AppointmentResponseMapper appointmentResponseMapper) {
    this.bookingContextLoader = bookingContextLoader;
    this.bookingRuleChecker = bookingRuleChecker;
    this.bookingRecorder = bookingRecorder;
    this.appointmentResponseMapper = appointmentResponseMapper;
  }

  /**
   * Books the slot for the signed-in customer, or returns the appointment an earlier request with
   * the same {@code idempotencyKey} created. Throws 409 with the failed rule's code, 404 for a
   * missing or inactive service.
   */
  @Transactional
  public AppointmentResponse book(Long customerId, BookingRequest request, UUID idempotencyKey) {
    Optional<Appointment> earlier = bookingRecorder.findEarlierResult(customerId, idempotencyKey);
    if (earlier.isPresent()) {
      return replay(earlier.get());
    }
    BookingContext context =
        bookingContextLoader.loadForBooking(
            request.staffId(), request.serviceId(), request.start().toInstant());
    Slot slot = bookingRuleChecker.checkRequest(context, customerId, request.start().toInstant());
    bookingRuleChecker.lockStaffMember(context);
    // A duplicate request that waited for the lock sees the first one's key before the count.
    Optional<Appointment> bookedMeanwhile =
        bookingRecorder.findEarlierResult(customerId, idempotencyKey);
    if (bookedMeanwhile.isPresent()) {
      return replay(bookedMeanwhile.get());
    }
    long estimatedWaitMinutes = bookingRuleChecker.checkCapacity(context, slot);
    Appointment appointment = bookingRecorder.record(customerId, context, slot, idempotencyKey);
    return appointmentResponseMapper.toResponse(appointment, context, estimatedWaitMinutes);
  }

  private AppointmentResponse replay(Appointment appointment) {
    BookingContext context = bookingContextLoader.loadForAppointment(appointment);
    return appointmentResponseMapper.toResponse(appointment, context, null);
  }
}
