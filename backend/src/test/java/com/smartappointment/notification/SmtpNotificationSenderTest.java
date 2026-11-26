package com.smartappointment.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.smartappointment.common.config.NotificationMailProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpNotificationSenderTest {

  private static final EmailMessage MESSAGE =
      new EmailMessage("alex.morgan@example.com", "Your appointment is booked", "Hello");

  @Mock private JavaMailSender mailSender;

  @Test
  void anAcceptedMailIsDeliveredFromTheConfiguredAddress() {
    DeliveryResult result = sender().send(MESSAGE);

    ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(sent.capture());
    assertThat(sent.getValue().getFrom()).isEqualTo("no-reply@smart-appointment.local");
    assertThat(sent.getValue().getTo()).containsExactly("alex.morgan@example.com");
    assertThat(result.isDelivered()).isTrue();
  }

  @Test
  void anSmtpFailureIsAFailedResultWithTheProvidersMessage() {
    doThrow(new MailSendException("Connection refused"))
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    DeliveryResult result = sender().send(MESSAGE);

    assertThat(result.isDelivered()).isFalse();
    assertThat(result.providerResponse()).contains("Connection refused");
  }

  private SmtpNotificationSender sender() {
    return new SmtpNotificationSender(
        mailSender, new NotificationMailProperties("no-reply@smart-appointment.local"));
  }
}
