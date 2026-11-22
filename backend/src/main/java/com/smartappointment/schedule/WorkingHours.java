package com.smartappointment.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

/** One staff member's shift, with an optional break, for one day of the week (SRS Table 9, F4). */
@Entity
@Table(name = "working_hours")
public class WorkingHours {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wh_id")
  private Long workingHoursId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "day_of_week", nullable = false)
  private short dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "break_start")
  private LocalTime breakStart;

  @Column(name = "break_end")
  private LocalTime breakEnd;

  protected WorkingHours() {}

  public WorkingHours(Long staffId, short dayOfWeek, LocalTime startTime, LocalTime endTime) {
    this.staffId = staffId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Long getWorkingHoursId() {
    return workingHoursId;
  }

  public Long getStaffId() {
    return staffId;
  }

  public short getDayOfWeek() {
    return dayOfWeek;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public LocalTime getBreakStart() {
    return breakStart;
  }

  public void setBreakStart(LocalTime breakStart) {
    this.breakStart = breakStart;
  }

  public LocalTime getBreakEnd() {
    return breakEnd;
  }

  public void setBreakEnd(LocalTime breakEnd) {
    this.breakEnd = breakEnd;
  }
}
