package com.smartappointment.notification;

/** Reason a {@link Notification} is sent (SRS Table 9, ck_notification_type). */
public enum NotificationType {
  CONFIRMATION,
  REMINDER,
  CANCELLATION,
  RESCHEDULE
}
