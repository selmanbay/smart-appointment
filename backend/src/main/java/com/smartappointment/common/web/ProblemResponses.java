package com.smartappointment.common.web;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/** Writes RFC 7807 bodies from servlet filters and security handlers, outside Spring MVC. */
public final class ProblemResponses {

  private static final String BODY =
      "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}";

  private ProblemResponses() {}

  public static void write(HttpServletResponse response, HttpStatus status, String detail)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.getWriter().write(BODY.formatted(status.getReasonPhrase(), status.value(), detail));
  }
}
