package com.smartappointment.estimation;

/**
 * One staff member to rank. {@code estimatedWaitMinutes} is the wait of their next free slot (SRS
 * Eq. 4), or {@code null} when they have no free slot that day; {@code meanRating} is {@code null}
 * when nobody has rated them yet.
 */
public record RankingCandidate(long staffId, Long estimatedWaitMinutes, Double meanRating) {

  public boolean hasFreeSlot() {
    return estimatedWaitMinutes != null;
  }
}
