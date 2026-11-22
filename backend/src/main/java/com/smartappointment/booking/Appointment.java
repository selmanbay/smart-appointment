package com.smartappointment.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/** A customer's booking of a staff member's time for a service (SRS Table 9, F5, F6, F8). */
@Entity
@Table(name = "appointment")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "appt_id")
  private Long appointmentId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "service_id", nullable = false)
  private Long serviceId;

  @Column(name = "start_time", nullable = false)
  private Instant startTime;

  @Column(name = "end_time", nullable = false)
  private Instant endTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private AppointmentStatus status = AppointmentStatus.BOOKED;

  @CreationTimestamp
  @Column(name = "booked_at", nullable = false)
  private Instant bookedAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  @Column(name = "checked_in_at")
  private Instant checkedInAt;

  @Column(name = "service_start_at")
  private Instant serviceStartAt;

  @Column(name = "service_end_at")
  private Instant serviceEndAt;

  protected Appointment() {}

  public Appointment(Long customerId, Long staffId, Long serviceId, Instant startTime) {
    this.customerId = customerId;
    this.staffId = staffId;
    this.serviceId = serviceId;
    this.startTime = startTime;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public Long getStaffId() {
    return staffId;
  }

  public Long getServiceId() {
    return serviceId;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void setStatus(AppointmentStatus status) {
    this.status = status;
  }

  public Instant getBookedAt() {
    return bookedAt;
  }

  public Instant getCancelledAt() {
    return cancelledAt;
  }

  public void setCancelledAt(Instant cancelledAt) {
    this.cancelledAt = cancelledAt;
  }

  public Instant getCheckedInAt() {
    return checkedInAt;
  }

  public void setCheckedInAt(Instant checkedInAt) {
    this.checkedInAt = checkedInAt;
  }

  public Instant getServiceStartAt() {
    return serviceStartAt;
  }

  public void setServiceStartAt(Instant serviceStartAt) {
    this.serviceStartAt = serviceStartAt;
  }

  public Instant getServiceEndAt() {
    return serviceEndAt;
  }

  public void setServiceEndAt(Instant serviceEndAt) {
    this.serviceEndAt = serviceEndAt;
  }
}
