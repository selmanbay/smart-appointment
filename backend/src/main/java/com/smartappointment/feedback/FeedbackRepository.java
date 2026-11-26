package com.smartappointment.feedback;

import com.smartappointment.feedback.dto.StaffRating;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence for {@link Feedback} (SRS Table 9). */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  Optional<Feedback> findByAppointmentId(Long appointmentId);

  boolean existsByAppointmentId(Long appointmentId);

  /**
   * Joined by entity name, so feedback does not depend on the booking package, which uses this
   * feature for staff ranking.
   */
  @Query(
      "select new com.smartappointment.feedback.dto.StaffRating(a.staffId, avg(f.rating))"
          + " from Feedback f join Appointment a on a.appointmentId = f.appointmentId"
          + " where a.staffId in :staffIds group by a.staffId")
  List<StaffRating> findMeanRatings(@Param("staffIds") Collection<Long> staffIds);
}
