package com.smartappointment.schedule;

/** Decision state of a {@link ScheduleChangeRequest} (SRS Table 9, ck_schedule_change_status). */
public enum ScheduleChangeStatus {
  PENDING,
  APPROVED,
  REJECTED
}
