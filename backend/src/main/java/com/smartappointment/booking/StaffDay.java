package com.smartappointment.booking;

import com.smartappointment.booking.dto.SlotResponse;
import java.util.List;

/**
 * One staff member's day for one service: the free slots in start order, the slots the day has (SRS
 * Eq. 1, capacity included) and how many of them are booked.
 */
record StaffDay(List<SlotResponse> freeSlots, int totalSlots, int bookedSlots) {

  static final StaffDay DAY_OFF = new StaffDay(List.of(), 0, 0);
}
