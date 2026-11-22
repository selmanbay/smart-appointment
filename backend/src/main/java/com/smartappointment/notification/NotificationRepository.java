package com.smartappointment.notification;

import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Notification} (SRS Table 9). */
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
