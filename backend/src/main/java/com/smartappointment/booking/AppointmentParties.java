package com.smartappointment.booking;

import com.smartappointment.booking.dto.AppointmentResponse.CustomerReference;
import com.smartappointment.booking.dto.AppointmentResponse.StaffReference;

/** Who an appointment is between, and whether the customer has rated it. */
record AppointmentParties(StaffReference staff, CustomerReference customer, boolean hasFeedback) {}
