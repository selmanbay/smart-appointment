package com.smartappointment.booking;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link BookingIdempotencyKey} (ADR-0018). */
public interface BookingIdempotencyKeyRepository
    extends JpaRepository<BookingIdempotencyKey, Long> {

  Optional<BookingIdempotencyKey> findByCustomerIdAndIdempotencyKey(
      Long customerId, UUID idempotencyKey);
}
