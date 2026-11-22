package com.smartappointment.booking;

import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Appointment} (SRS Table 9). */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {}
