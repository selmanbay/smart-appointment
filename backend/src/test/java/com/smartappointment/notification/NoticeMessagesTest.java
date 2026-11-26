package com.smartappointment.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartappointment.booking.AppointmentEvent.Change;
import com.smartappointment.booking.dto.AppointmentNotice;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class NoticeMessagesTest {

  private static final AppointmentNotice NOTICE =
      new AppointmentNotice(
          42L,
          "alex.morgan@example.com",
          "Alex Morgan",
          "Academic advising",
          "Maya Chen",
          OffsetDateTime.parse("2026-11-30T09:30+03:00"));

  @Test
  void aConfirmationNamesTheServiceTheStaffMemberAndTheLocalTime() {
    EmailMessage message = NoticeMessages.compose(Change.BOOKED, NOTICE);

    assertThat(message.to()).isEqualTo("alex.morgan@example.com");
    assertThat(message.subject()).isEqualTo("Your appointment is booked");
    assertThat(message.body())
        .contains("Hello Alex Morgan,")
        .contains("Academic advising with Maya Chen")
        .contains("Monday 30 November 2026, 09:30");
  }

  @Test
  void eachChangeHasItsOwnSubjectAndNotificationType() {
    assertThat(NoticeMessages.compose(Change.RESCHEDULED, NOTICE).subject())
        .isEqualTo("Your appointment has moved");
    assertThat(NoticeMessages.compose(Change.CANCELLED, NOTICE).subject())
        .isEqualTo("Your appointment is cancelled");
    assertThat(NoticeMessages.typeOf(Change.BOOKED)).isEqualTo(NotificationType.CONFIRMATION);
    assertThat(NoticeMessages.typeOf(Change.RESCHEDULED)).isEqualTo(NotificationType.RESCHEDULE);
    assertThat(NoticeMessages.typeOf(Change.CANCELLED)).isEqualTo(NotificationType.CANCELLATION);
  }
}
