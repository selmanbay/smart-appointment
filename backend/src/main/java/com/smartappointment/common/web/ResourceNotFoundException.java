package com.smartappointment.common.web;

/** Thrown by services when a requested record does not exist; mapped to 404. */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String resourceName, Object resourceId) {
    super(resourceName + " " + resourceId + " was not found");
  }
}
