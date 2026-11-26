package com.smartappointment.notification;

/**
 * Sends a message through an external provider (AGENT_RULES section 14): SMTP now, push (FCM)
 * later, a fake in tests. Implementations report a provider failure as a failed result instead of
 * throwing, so the caller can record it.
 */
public interface NotificationSender {

  DeliveryResult send(EmailMessage message);
}
