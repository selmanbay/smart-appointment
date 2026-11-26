package com.smartappointment.notification;

/**
 * What the provider answered for one message, stored in {@code notification.provider_response} (SRS
 * Table 9).
 */
public record DeliveryResult(boolean isDelivered, String providerResponse) {

  public static DeliveryResult delivered(String providerResponse) {
    return new DeliveryResult(true, providerResponse);
  }

  public static DeliveryResult failed(String providerResponse) {
    return new DeliveryResult(false, providerResponse);
  }
}
