package com.smartappointment.estimation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class StaffRankingTest {

  @Test
  void theShortestEstimatedWaitComesFirst() {
    List<RankingCandidate> ranked =
        StaffRanking.rank(
            List.of(
                new RankingCandidate(1, 15L, 4.9),
                new RankingCandidate(2, 5L, 4.1),
                new RankingCandidate(3, 10L, null)));

    assertThat(ranked).extracting(RankingCandidate::staffId).containsExactly(2L, 3L, 1L);
  }

  @Test
  void anEqualWaitIsBrokenByTheHigherMeanRating() {
    List<RankingCandidate> ranked =
        StaffRanking.rank(
            List.of(new RankingCandidate(1, 5L, 4.7), new RankingCandidate(2, 5L, 4.8)));

    assertThat(ranked).extracting(RankingCandidate::staffId).containsExactly(2L, 1L);
  }

  @Test
  void staffWithoutARatingComeAfterRatedStaffWithTheSameWait() {
    List<RankingCandidate> ranked =
        StaffRanking.rank(
            List.of(new RankingCandidate(1, 5L, null), new RankingCandidate(2, 5L, 1.0)));

    assertThat(ranked).extracting(RankingCandidate::staffId).containsExactly(2L, 1L);
  }

  @Test
  void staffWithoutAFreeSlotComeLast() {
    List<RankingCandidate> ranked =
        StaffRanking.rank(
            List.of(new RankingCandidate(1, null, 5.0), new RankingCandidate(2, 60L, null)));

    assertThat(ranked).extracting(RankingCandidate::staffId).containsExactly(2L, 1L);
  }

  @Test
  void aFullTieKeepsStaffIdOrder() {
    List<RankingCandidate> ranked =
        StaffRanking.rank(
            List.of(new RankingCandidate(7, 0L, 4.5), new RankingCandidate(3, 0L, 4.5)));

    assertThat(ranked).extracting(RankingCandidate::staffId).containsExactly(3L, 7L);
  }
}
