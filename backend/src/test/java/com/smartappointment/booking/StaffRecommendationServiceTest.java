package com.smartappointment.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.smartappointment.booking.dto.SlotResponse;
import com.smartappointment.booking.dto.StaffOptionResponse;
import com.smartappointment.catalog.CatalogService;
import com.smartappointment.catalog.StaffLookupService;
import com.smartappointment.catalog.dto.StaffMemberSummary;
import com.smartappointment.common.web.ResourceNotFoundException;
import com.smartappointment.feedback.FeedbackService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffRecommendationServiceTest {

  private static final long ADVISING = 1L;
  private static final LocalDate MONDAY = LocalDate.of(2026, 11, 30);
  private static final OffsetDateTime NINE = OffsetDateTime.parse("2026-11-30T09:00+03:00");

  @Mock private CatalogService catalogService;
  @Mock private StaffLookupService staffLookupService;
  @Mock private FeedbackService feedbackService;
  @Mock private SlotService slotService;

  @Test
  void ranksEveryActiveProviderByTheWaitOfTheirNextFreeSlot() {
    when(staffLookupService.findActiveProviders(ADVISING))
        .thenReturn(
            List.of(
                new StaffMemberSummary(1L, "Maya Chen", null, 1),
                new StaffMemberSummary(2L, "Daniel Brooks", null, 1)));
    when(feedbackService.findMeanRatingByStaffId(any())).thenReturn(Map.of(1L, 4.7, 2L, 4.8));
    when(slotService.staffDay(1L, ADVISING, MONDAY)).thenReturn(dayWithWait(5));
    when(slotService.staffDay(2L, ADVISING, MONDAY)).thenReturn(dayWithWait(15));

    List<StaffOptionResponse> options = service().rankStaff(ADVISING, MONDAY);

    assertThat(options)
        .extracting(StaffOptionResponse::fullName)
        .containsExactly("Maya Chen", "Daniel Brooks");
    assertThat(options).extracting(StaffOptionResponse::isRecommended).containsExactly(true, false);
  }

  @Test
  void aMissingOrInactiveServiceIsNotFound() {
    when(catalogService.getActiveService(9L))
        .thenThrow(new ResourceNotFoundException("Service", 9L));

    assertThatThrownBy(() -> service().rankStaff(9L, MONDAY))
        .isInstanceOf(ResourceNotFoundException.class);
    verifyNoInteractions(slotService);
  }

  @Test
  void aServiceNobodyProvidesHasNoOptions() {
    when(staffLookupService.findActiveProviders(ADVISING)).thenReturn(List.of());

    assertThat(service().rankStaff(ADVISING, MONDAY)).isEmpty();
  }

  private StaffRecommendationService service() {
    return new StaffRecommendationService(
        catalogService, staffLookupService, feedbackService, slotService);
  }

  private static StaffDay dayWithWait(long estimatedWaitMinutes) {
    return new StaffDay(
        List.of(new SlotResponse(NINE, NINE.plusMinutes(30), 1, estimatedWaitMinutes)), 12, 0);
  }
}
