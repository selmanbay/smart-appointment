package com.smartappointment.booking.dto;

import com.smartappointment.common.web.PageMetadata;
import java.util.List;

/** One page of appointments (contract: AppointmentPage). */
public record AppointmentPageResponse(List<AppointmentResponse> content, PageMetadata page) {}
