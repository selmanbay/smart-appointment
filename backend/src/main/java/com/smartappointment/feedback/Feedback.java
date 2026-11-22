package com.smartappointment.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/** A customer's rating and comment for one completed appointment (SRS Table 9, F11, UC-7). */
@Entity
@Table(name = "feedback")
public class Feedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "feedback_id")
  private Long feedbackId;

  @Column(name = "appt_id", nullable = false, unique = true)
  private Long appointmentId;

  @Column(name = "rating", nullable = false)
  private short rating;

  @Column(name = "comment")
  private String comment;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Feedback() {}

  public Feedback(Long appointmentId, short rating) {
    this.appointmentId = appointmentId;
    this.rating = rating;
  }

  public Long getFeedbackId() {
    return feedbackId;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public short getRating() {
    return rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
