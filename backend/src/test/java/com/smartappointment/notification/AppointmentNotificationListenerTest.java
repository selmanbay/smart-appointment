package com.smartappointment.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartappointment.booking.AppointmentEvent;
import com.smartappointment.booking.AppointmentNoticeService;
import com.smartappointment.booking.dto.AppointmentNotice;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Every attempt is recorded with its status and the provider's answer (UC-20). */
@ExtendWith(MockitoExtension.class)
class AppointmentNotificationListenerTest {

  private static final Instant NOW = Instant.parse("2026-11-26T07:00:00Z");
  private static final AppointmentEvent BOOKED =
      new AppointmentEvent(42L, AppointmentEvent.Change.BOOKED);

  @Mock private AppointmentNoticeService appointmentNoticeService;
  @Mock private NotificationSender notificationSender;
  @Mock private NotificationRepository notificationRepository;

  @BeforeEach
  void givenTheNotice() {
    when(appointmentNoticeService.noticeFor(42L))
        .thenReturn(
            new AppointmentNotice(
                42L,
                "alex.morgan@example.com",
                "Alex Morgan",
                "Academic advising",
                "Maya Chen",
                OffsetDateTime.parse("2026-11-30T09:30+03:00")));
  }

  @Test
  void aDeliveredMailIsRecordedAsSent() {
    when(notificationSender.send(any())).thenReturn(DeliveryResult.delivered("250 OK"));

    Notification notification = handle(BOOKED);

    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(notification.getSentAt()).isEqualTo(NOW);
    assertThat(notification.getType()).isEqualTo(NotificationType.CONFIRMATION);
    assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
    assertThat(notification.getProviderResponse()).isEqualTo("250 OK");
  }

  @Test
  void aFailedMailIsRecordedAsFailedWithTheAnswerShortenedToTheColumn() {
    when(notificationSender.send(any())).thenReturn(DeliveryResult.failed("x".repeat(1200)));

    Notification notification =
        handle(new AppointmentEvent(42L, AppointmentEvent.Change.CANCELLED));

    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    assertThat(notification.getSentAt()).isNull();
    assertThat(notification.getType()).isEqualTo(NotificationType.CANCELLATION);
    assertThat(notification.getProviderResponse()).hasSize(1000);
  }

  private Notification handle(AppointmentEvent event) {
    new AppointmentNotificationListener(
            appointmentNoticeService,
            notificationSender,
            notificationRepository,
            Clock.fixed(NOW, ZoneOffset.UTC))
        .onAppointmentChange(event);
    ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(saved.capture());
    return saved.getValue();
  }
}
