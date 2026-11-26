package com.smartappointment.notification;

/** A plain-text e-mail to one recipient. */
public record EmailMessage(String to, String subject, String body) {}
