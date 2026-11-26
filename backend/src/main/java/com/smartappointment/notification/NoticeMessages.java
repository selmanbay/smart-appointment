package com.smartappointment.notification;

import com.smartappointment.booking.AppointmentEvent.Change;
import com.smartappointment.booking.dto.AppointmentNotice;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/** The e-mail a customer gets for each appointment change (UC-20), and its notification type. */
final class NoticeMessages {

  private static final DateTimeFormatter START_FORMAT =
      DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale.ENGLISH);

  private static final Map<Change, NotificationType> TYPES =
      Map.of(
          Change.BOOKED, NotificationType.CONFIRMATION,
          Change.RESCHEDULED, NotificationType.RESCHEDULE,
          Change.CANCELLED, NotificationType.CANCELLATION);

  private static final Map<Change, NoticeText> TEXTS =
      Map.of(
          Change.BOOKED,
          new NoticeText("Your appointment is booked", "Your appointment is booked."),
          Change.RESCHEDULED,
          new NoticeText("Your appointment has moved", "Your appointment now takes place at:"),
          Change.CANCELLED,
          new NoticeText("Your appointment is cancelled", "This appointment is cancelled:"));

  private NoticeMessages() {}

  static NotificationType typeOf(Change change) {
    return TYPES.get(change);
  }

  static EmailMessage compose(Change change, AppointmentNotice notice) {
    NoticeText text = TEXTS.get(change);
    String body =
        """
        Hello %s,

        %s

        %s with %s
        %s

        Smart Appointment
        """
            .formatted(
                notice.customerName(),
                text.opening(),
                notice.serviceName(),
                notice.staffName(),
                notice.start().format(START_FORMAT));
    return new EmailMessage(notice.customerEmail(), text.subject(), body);
  }

  private record NoticeText(String subject, String opening) {}
}
