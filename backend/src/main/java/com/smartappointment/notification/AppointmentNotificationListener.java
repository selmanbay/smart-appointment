package com.smartappointment.notification;

import com.smartappointment.booking.AppointmentEvent;
import com.smartappointment.booking.AppointmentNoticeService;
import com.smartappointment.booking.dto.AppointmentNotice;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * E-mails the customer about a booking, a reschedule or a cancellation (UC-20) once the change has
 * committed, and records every attempt in {@code notification} with its status and the provider's
 * answer. A failed send is recorded as FAILED; it never undoes the appointment (AGENT_RULES section
 * 8), because the appointment's transaction is already committed.
 */
@Component
class AppointmentNotificationListener {

  /** The width of {@code notification.provider_response} (SRS Table 9). */
  private static final int PROVIDER_RESPONSE_LENGTH = 1000;

  private final AppointmentNoticeService appointmentNoticeService;
  private final NotificationSender notificationSender;
  private final NotificationRepository notificationRepository;
  private final Clock clock;

  AppointmentNotificationListener(
      AppointmentNoticeService appointmentNoticeService,
      NotificationSender notificationSender,
      NotificationRepository notificationRepository,
      Clock clock) {
    this.appointmentNoticeService = appointmentNoticeService;
    this.notificationSender = notificationSender;
    this.notificationRepository = notificationRepository;
    this.clock = clock;
  }

  /** Runs after commit in its own transaction, so the record is written even if sending fails. */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onAppointmentChange(AppointmentEvent event) {
    AppointmentNotice notice = appointmentNoticeService.noticeFor(event.appointmentId());
    Instant now = clock.instant();
    Notification notification =
        new Notification(
            event.appointmentId(),
            NotificationChannel.EMAIL,
            NoticeMessages.typeOf(event.change()),
            now);
    DeliveryResult result = notificationSender.send(NoticeMessages.compose(event.change(), notice));
    notification.setStatus(
        result.isDelivered() ? NotificationStatus.SENT : NotificationStatus.FAILED);
    notification.setSentAt(result.isDelivered() ? now : null);
    notification.setProviderResponse(shortened(result.providerResponse()));
    notificationRepository.save(notification);
  }

  private static String shortened(String providerResponse) {
    if (providerResponse == null || providerResponse.length() <= PROVIDER_RESPONSE_LENGTH) {
      return providerResponse;
    }
    return providerResponse.substring(0, PROVIDER_RESPONSE_LENGTH);
  }
}
