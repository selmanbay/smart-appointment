package com.smartappointment.feedback;

import static java.util.stream.Collectors.toMap;

import com.smartappointment.feedback.dto.StaffRating;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ratings other features read: the tie-break in staff ranking (UC-18), feedback flags. */
@Service
public class FeedbackService {

  private final FeedbackRepository feedbackRepository;

  public FeedbackService(FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
  }

  /** Staff members without any rating are absent from the map. */
  @Transactional(readOnly = true)
  public Map<Long, Double> findMeanRatingByStaffId(Collection<Long> staffIds) {
    if (staffIds.isEmpty()) {
      return Map.of();
    }
    return feedbackRepository.findMeanRatings(staffIds).stream()
        .collect(toMap(StaffRating::staffId, StaffRating::meanRating));
  }

  @Transactional(readOnly = true)
  public boolean hasFeedback(Long appointmentId) {
    return feedbackRepository.existsByAppointmentId(appointmentId);
  }
}
