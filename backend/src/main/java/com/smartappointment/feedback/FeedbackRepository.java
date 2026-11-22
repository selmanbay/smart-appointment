package com.smartappointment.feedback;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Feedback} (SRS Table 9). */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  Optional<Feedback> findByAppointmentId(Long appointmentId);
}
