package com.smartappointment.estimation;

import java.time.Duration;
import java.time.Instant;

/** One staff member's appointment in start-time order, as input to {@link WaitingTimeEstimator}. */
public record ScheduledService(Instant scheduledStart, Duration learnedDuration) {}
