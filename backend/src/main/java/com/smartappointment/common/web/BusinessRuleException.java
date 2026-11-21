package com.smartappointment.common.web;

/**
 * Thrown by services when a request breaks a business rule (capacity full, outside working hours,
 * past the cancellation deadline); mapped to 409 with the rule's message.
 */
public class BusinessRuleException extends RuntimeException {

  public BusinessRuleException(String message) {
    super(message);
  }
}
