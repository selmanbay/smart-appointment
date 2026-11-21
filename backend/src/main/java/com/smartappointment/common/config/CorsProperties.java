package com.smartappointment.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Browser origins allowed to call the API. Set with CORS_ALLOWED_ORIGINS. */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

  public CorsProperties {
    allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
  }
}
