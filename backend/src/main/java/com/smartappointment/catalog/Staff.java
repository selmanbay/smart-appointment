package com.smartappointment.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/** A staff member who provides one or more {@link Service}s (SRS Table 9, F3). */
@Entity
@Table(name = "staff")
public class Staff {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "staff_id")
  private Long staffId;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "title", length = 100)
  private String title;

  @Column(name = "capacity", nullable = false)
  private int capacity = 1;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @ManyToMany
  @JoinTable(
      name = "staff_service",
      joinColumns = @JoinColumn(name = "staff_id"),
      inverseJoinColumns = @JoinColumn(name = "service_id"))
  private Set<Service> services = new HashSet<>();

  protected Staff() {}

  public Staff(Long userId, int capacity) {
    this.userId = userId;
    this.capacity = capacity;
  }

  public Long getStaffId() {
    return staffId;
  }

  public Long getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }

  public Set<Service> getServices() {
    return services;
  }
}
