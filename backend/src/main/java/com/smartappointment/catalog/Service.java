package com.smartappointment.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Something an organization offers for booking, with an expected and a learned duration (SRS Table
 * 9, F3, F9).
 */
@Entity
@Table(name = "service")
public class Service {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "service_id")
  private Long serviceId;

  @Column(name = "org_id", nullable = false)
  private Long organizationId;

  @Column(name = "name", length = 200, nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "expected_duration_min", nullable = false)
  private int expectedDurationMin;

  @Column(name = "learned_duration_min", precision = 7, scale = 2)
  private BigDecimal learnedDurationMin;

  @Column(name = "buffer_min", nullable = false)
  private int bufferMin;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  protected Service() {}

  public Service(Long organizationId, String name, int expectedDurationMin) {
    this.organizationId = organizationId;
    this.name = name;
    this.expectedDurationMin = expectedDurationMin;
  }

  public Long getServiceId() {
    return serviceId;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getExpectedDurationMin() {
    return expectedDurationMin;
  }

  public void setExpectedDurationMin(int expectedDurationMin) {
    this.expectedDurationMin = expectedDurationMin;
  }

  public BigDecimal getLearnedDurationMin() {
    return learnedDurationMin;
  }

  public void setLearnedDurationMin(BigDecimal learnedDurationMin) {
    this.learnedDurationMin = learnedDurationMin;
  }

  public int getBufferMin() {
    return bufferMin;
  }

  public void setBufferMin(int bufferMin) {
    this.bufferMin = bufferMin;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }
}
