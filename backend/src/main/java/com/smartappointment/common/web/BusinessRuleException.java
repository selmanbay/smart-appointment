package com.smartappointment.common.web;

/**
 * Thrown by services when a request breaks a business rule (capacity full, outside working hours,
 * past the cancellation deadline); mapped to 409. A {@code code} tells clients which rule failed,
 * for example the contract's {@code BookingProblem.code}.
 */
public class BusinessRuleException extends RuntimeException {

  private final String code;
  private final String title;

  public BusinessRuleException(String message) {
    this(null, null, message);
  }

  public BusinessRuleException(String code, String title, String message) {
    super(message);
    this.code = code;
    this.title = title;
  }

  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }
}
