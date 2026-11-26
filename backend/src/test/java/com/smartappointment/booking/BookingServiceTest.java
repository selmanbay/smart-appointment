package com.smartappointment.booking;

import static com.smartappointment.booking.SlotFixtures.MONDAY;
import static com.smartappointment.booking.SlotFixtures.NO_RULE;
import static com.smartappointment.booking.SlotFixtures.advising;
import static com.smartappointment.booking.SlotFixtures.localTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.booking.dto.AppointmentResponse;
import com.smartappointment.booking.dto.BookingRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** The Idempotency-Key paths of bookAppointment (contract, ADR-0018). */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  private static final long CUSTOMER_ID = 9L;
  private static final UUID KEY = UUID.fromString("7d0f4c58-4c2f-4d8e-9a3e-0f6b8a1c2d3e");
  private static final Instant MONDAY_NINE = localTime(MONDAY, 9, 0);
  private static final BookingRequest REQUEST =
      new BookingRequest(1L, 1L, MONDAY_NINE.atZone(SlotFixtures.ISTANBUL).toOffsetDateTime());

  @Mock private BookingContextLoader bookingContextLoader;
  @Mock private BookingRuleChecker bookingRuleChecker;
  @Mock private BookingRecorder bookingRecorder;
  @Mock private AppointmentResponseMapper appointmentResponseMapper;

  @Test
  void aNewRequestIsCheckedAndRecorded() {
    BookingContext context = advising(1, NO_RULE);
    Slot slot = new Slot(MONDAY_NINE, localTime(MONDAY, 9, 30));
    Appointment recorded = new Appointment(CUSTOMER_ID, 1L, 1L, MONDAY_NINE);
    AppointmentResponse response = mock(AppointmentResponse.class);
    when(bookingRecorder.findEarlierResult(CUSTOMER_ID, KEY)).thenReturn(Optional.empty());
    when(bookingContextLoader.loadForBooking(1L, 1L, MONDAY_NINE)).thenReturn(context);
    when(bookingRuleChecker.checkRequest(context, CUSTOMER_ID, MONDAY_NINE)).thenReturn(slot);
    when(bookingRuleChecker.checkCapacity(context, slot)).thenReturn(4L);
    when(bookingRecorder.record(CUSTOMER_ID, context, slot, KEY)).thenReturn(recorded);
    when(appointmentResponseMapper.toResponse(recorded, context, 4L)).thenReturn(response);

    assertThat(service().book(CUSTOMER_ID, REQUEST, KEY)).isSameAs(response);
  }

  @Test
  void aRepeatedKeyReturnsTheFirstAppointmentWithoutCheckingAgain() {
    Appointment earlier = new Appointment(CUSTOMER_ID, 1L, 1L, MONDAY_NINE);
    when(bookingRecorder.findEarlierResult(CUSTOMER_ID, KEY)).thenReturn(Optional.of(earlier));
    when(bookingContextLoader.loadForAppointment(earlier)).thenReturn(advising(1, NO_RULE));

    service().book(CUSTOMER_ID, REQUEST, KEY);

    verifyNoInteractions(bookingRuleChecker);
    verify(bookingRecorder, never()).record(any(), any(), any(), any());
    verify(appointmentResponseMapper).toResponse(earlier, advising(1, NO_RULE), null);
  }

  @Test
  void aDuplicateThatWaitedForTheLockReturnsTheFirstAppointment() {
    BookingContext context = advising(1, NO_RULE);
    Appointment first = new Appointment(CUSTOMER_ID, 1L, 1L, MONDAY_NINE);
    when(bookingRecorder.findEarlierResult(CUSTOMER_ID, KEY))
        .thenReturn(Optional.empty(), Optional.of(first));
    when(bookingContextLoader.loadForBooking(1L, 1L, MONDAY_NINE)).thenReturn(context);
    when(bookingContextLoader.loadForAppointment(first)).thenReturn(context);
    when(bookingRuleChecker.checkRequest(context, CUSTOMER_ID, MONDAY_NINE))
        .thenReturn(new Slot(MONDAY_NINE, localTime(MONDAY, 9, 30)));

    service().book(CUSTOMER_ID, REQUEST, KEY);

    verify(bookingRuleChecker).lockStaffMember(context);
    verify(bookingRuleChecker, never()).checkCapacity(any(), any());
    verify(bookingRecorder, never()).record(any(), any(), any(), any());
    verify(appointmentResponseMapper).toResponse(first, context, null);
  }

  private BookingService service() {
    return new BookingService(
        bookingContextLoader, bookingRuleChecker, bookingRecorder, appointmentResponseMapper);
  }
}
