package com.smartappointment.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Booking limits for one service: lead time, advance window, cancellation deadline (SRS Table 9,
 * F4).
 */
@Entity
@Table(name = "appointment_rule")
public class AppointmentRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rule_id")
  private Long ruleId;

  @Column(name = "service_id", nullable = false, unique = true)
  private Long serviceId;

  @Column(name = "max_advance_days")
  private Integer maxAdvanceDays;

  @Column(name = "min_lead_hours")
  private Integer minLeadHours;

  @Column(name = "cancel_deadline_hours")
  private Integer cancelDeadlineHours;

  @Column(name = "max_active_per_user")
  private Integer maxActivePerUser;

  protected AppointmentRule() {}

  public AppointmentRule(Long serviceId) {
    this.serviceId = serviceId;
  }

  public Long getRuleId() {
    return ruleId;
  }

  public Long getServiceId() {
    return serviceId;
  }

  public Integer getMaxAdvanceDays() {
    return maxAdvanceDays;
  }

  public void setMaxAdvanceDays(Integer maxAdvanceDays) {
    this.maxAdvanceDays = maxAdvanceDays;
  }

  public Integer getMinLeadHours() {
    return minLeadHours;
  }

  public void setMinLeadHours(Integer minLeadHours) {
    this.minLeadHours = minLeadHours;
  }

  public Integer getCancelDeadlineHours() {
    return cancelDeadlineHours;
  }

  public void setCancelDeadlineHours(Integer cancelDeadlineHours) {
    this.cancelDeadlineHours = cancelDeadlineHours;
  }

  public Integer getMaxActivePerUser() {
    return maxActivePerUser;
  }

  public void setMaxActivePerUser(Integer maxActivePerUser) {
    this.maxActivePerUser = maxActivePerUser;
  }
}
