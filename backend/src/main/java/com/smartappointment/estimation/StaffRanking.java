package com.smartappointment.estimation;

import java.util.Comparator;
import java.util.List;

/**
 * Orders staff for a booking (SRS F9, UC-18, UC-19): shortest estimated wait first, ties broken by
 * the higher mean rating, unrated staff after rated ones, staff without a free slot last. Staff id
 * breaks any remaining tie so the order is stable between requests.
 */
public final class StaffRanking {

  private static final Comparator<RankingCandidate> ORDER =
      Comparator.comparing(
              RankingCandidate::estimatedWaitMinutes,
              Comparator.nullsLast(Comparator.<Long>naturalOrder()))
          .thenComparing(
              RankingCandidate::meanRating, Comparator.nullsLast(Comparator.<Double>reverseOrder()))
          .thenComparingLong(RankingCandidate::staffId);

  private StaffRanking() {}

  public static List<RankingCandidate> rank(List<RankingCandidate> candidates) {
    return candidates.stream().sorted(ORDER).toList();
  }
}
