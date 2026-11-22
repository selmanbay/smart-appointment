package com.smartappointment.estimation;

import java.time.Duration;
import java.time.Instant;

/** The projected start and finish of one appointment, and the resulting wait (SRS Eq. 4). */
public record WaitingTimeEstimate(
    Instant projectedStart, Instant projectedFinish, Duration waitingTime) {}
