package com.smartappointment.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One attempt to notify a customer or staff member about an appointment (SRS Table 9, F7). */
@Entity
@Table(name = "notification")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notif_id")
  private Long notificationId;

  @Column(name = "appt_id", nullable = false)
  private Long appointmentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel", length = 10, nullable = false)
  private NotificationChannel channel;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 30, nullable = false)
  private NotificationType type;

  @Column(name = "scheduled_for", nullable = false)
  private Instant scheduledFor;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private NotificationStatus status = NotificationStatus.PENDING;

  @Column(name = "provider_response", length = 1000)
  private String providerResponse;

  protected Notification() {}

  public Notification(
      Long appointmentId,
      NotificationChannel channel,
      NotificationType type,
      Instant scheduledFor) {
    this.appointmentId = appointmentId;
    this.channel = channel;
    this.type = type;
    this.scheduledFor = scheduledFor;
  }

  public Long getNotificationId() {
    return notificationId;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public NotificationChannel getChannel() {
    return channel;
  }

  public NotificationType getType() {
    return type;
  }

  public Instant getScheduledFor() {
    return scheduledFor;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public void setSentAt(Instant sentAt) {
    this.sentAt = sentAt;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public void setStatus(NotificationStatus status) {
    this.status = status;
  }

  public String getProviderResponse() {
    return providerResponse;
  }

  public void setProviderResponse(String providerResponse) {
    this.providerResponse = providerResponse;
  }
}
