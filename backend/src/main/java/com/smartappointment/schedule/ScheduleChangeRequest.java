package com.smartappointment.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A staff member's request to change their shift on one day of the week (SRS Table 9, F4, UC-10).
 */
@Entity
@Table(name = "schedule_change_request")
public class ScheduleChangeRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "request_id")
  private Long requestId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "day_of_week", nullable = false)
  private short dayOfWeek;

  @Column(name = "new_start_time", nullable = false)
  private LocalTime newStartTime;

  @Column(name = "new_end_time", nullable = false)
  private LocalTime newEndTime;

  @CreationTimestamp
  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private ScheduleChangeStatus status = ScheduleChangeStatus.PENDING;

  @Column(name = "decided_at")
  private Instant decidedAt;

  protected ScheduleChangeRequest() {}

  public ScheduleChangeRequest(
      Long staffId, short dayOfWeek, LocalTime newStartTime, LocalTime newEndTime) {
    this.staffId = staffId;
    this.dayOfWeek = dayOfWeek;
    this.newStartTime = newStartTime;
    this.newEndTime = newEndTime;
  }

  public Long getRequestId() {
    return requestId;
  }

  public Long getStaffId() {
    return staffId;
  }

  public short getDayOfWeek() {
    return dayOfWeek;
  }

  public LocalTime getNewStartTime() {
    return newStartTime;
  }

  public LocalTime getNewEndTime() {
    return newEndTime;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public ScheduleChangeStatus getStatus() {
    return status;
  }

  public void setStatus(ScheduleChangeStatus status) {
    this.status = status;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(Instant decidedAt) {
    this.decidedAt = decidedAt;
  }
}
