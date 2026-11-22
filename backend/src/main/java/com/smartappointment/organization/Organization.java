package com.smartappointment.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A tenant of the system (SRS Table 9, F2). */
@Entity
@Table(name = "organization")
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "org_id")
  private Long organizationId;

  @Column(name = "name", length = 200, nullable = false)
  private String name;

  @Column(name = "address", length = 500)
  private String address;

  @Column(name = "timezone", length = 64, nullable = false)
  private String timezone;

  @Column(name = "logo", length = 500)
  private String logo;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "terminology", nullable = false)
  private Map<String, String> terminology = new HashMap<>();

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  protected Organization() {}

  public Organization(String name, String timezone) {
    this.name = name;
    this.timezone = timezone;
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

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLogo() {
    return logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public Map<String, String> getTerminology() {
    return terminology;
  }

  public void setTerminology(Map<String, String> terminology) {
    this.terminology = terminology;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }
}
