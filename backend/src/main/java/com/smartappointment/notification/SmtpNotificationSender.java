package com.smartappointment.notification;

import com.smartappointment.common.config.NotificationMailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** E-mail over SMTP (Mailpit locally, any SMTP relay when hosted). */
@Component
class SmtpNotificationSender implements NotificationSender {

  private static final String ACCEPTED = "Accepted by the SMTP server";

  private final JavaMailSender mailSender;
  private final NotificationMailProperties properties;

  SmtpNotificationSender(JavaMailSender mailSender, NotificationMailProperties properties) {
    this.mailSender = mailSender;
    this.properties = properties;
  }

  @Override
  public DeliveryResult send(EmailMessage message) {
    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setFrom(properties.from());
    mail.setTo(message.to());
    mail.setSubject(message.subject());
    mail.setText(message.body());
    try {
      mailSender.send(mail);
      return DeliveryResult.delivered(ACCEPTED);
    } catch (MailException exception) {
      return DeliveryResult.failed(exception.getMessage());
    }
  }
}
