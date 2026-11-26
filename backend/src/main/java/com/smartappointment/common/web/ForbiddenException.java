package com.smartappointment.common.web;

/**
 * Thrown by services when the signed-in account may not act on a record it can see (for example
 * another customer's appointment); mapped to 403.
 */
public class ForbiddenException extends RuntimeException {

  public ForbiddenException(String message) {
    super(message);
  }
}
