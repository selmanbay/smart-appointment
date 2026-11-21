package com.smartappointment.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  @Test
  void reusesValidIncomingId() {
    assertThat(CorrelationIdFilter.resolveCorrelationId("abc-12345")).isEqualTo("abc-12345");
  }

  @Test
  void replacesMissingOrUnsafeIdWithUuid() {
    String generated = CorrelationIdFilter.resolveCorrelationId("<script>");

    assertThat(UUID.fromString(generated)).isNotNull();
    assertThat(UUID.fromString(CorrelationIdFilter.resolveCorrelationId(null))).isNotNull();
  }

  @Test
  void echoesIdInResponseHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/services");
    request.addHeader(CorrelationIdFilter.HEADER, "request-0001");
    MockHttpServletResponse response = new MockHttpServletResponse();

    new CorrelationIdFilter().doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("request-0001");
  }
}
