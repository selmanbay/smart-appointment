package com.smartappointment.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sender address of outgoing e-mail (UC-20). Set with MAIL_FROM; the SMTP host, port and time-outs
 * are Spring's own {@code spring.mail.*} settings (MAIL_HOST, MAIL_PORT, MAIL_TIMEOUT_MS).
 */
@ConfigurationProperties(prefix = "app.mail")
public record NotificationMailProperties(String from) {

  public NotificationMailProperties {
    if (from == null || from.isBlank()) {
      throw new IllegalStateException("app.mail.from (MAIL_FROM) must be set");
    }
  }
}
