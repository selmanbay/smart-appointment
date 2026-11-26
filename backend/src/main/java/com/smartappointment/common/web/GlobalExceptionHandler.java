package com.smartappointment.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns domain exceptions into RFC 7807 problem responses. Framework errors are handled by the base
 * class.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** Which rule failed, for example {@code SLOT_TAKEN} (contract: BookingProblem.code). */
  static final String CODE_PROPERTY = "code";

  @ExceptionHandler(ResourceNotFoundException.class)
  ProblemDetail handleNotFound(ResourceNotFoundException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  ProblemDetail handleForbidden(ForbiddenException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
  }

  @ExceptionHandler(BusinessRuleException.class)
  ProblemDetail handleBusinessRule(BusinessRuleException exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    if (exception.getTitle() != null) {
      problem.setTitle(exception.getTitle());
    }
    if (exception.getCode() != null) {
      problem.setProperty(CODE_PROPERTY, exception.getCode());
    }
    return problem;
  }

  /** Same answer for an unknown e-mail and a wrong password (ADR-0015). */
  @ExceptionHandler(BadCredentialsException.class)
  ProblemDetail handleBadCredentials(BadCredentialsException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }
}
