package com.smartappointment.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/** The appointment a customer's booking request with an Idempotency-Key created (ADR-0018). */
@Entity
@Table(name = "booking_idempotency_key")
public class BookingIdempotencyKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "key_id")
  private Long keyId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "idempotency_key", nullable = false)
  private UUID idempotencyKey;

  @Column(name = "appt_id", nullable = false)
  private Long appointmentId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected BookingIdempotencyKey() {}

  public BookingIdempotencyKey(Long customerId, UUID idempotencyKey, Long appointmentId) {
    this.customerId = customerId;
    this.idempotencyKey = idempotencyKey;
    this.appointmentId = appointmentId;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }
}
