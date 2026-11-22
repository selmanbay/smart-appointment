package com.smartappointment.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Service} (SRS Table 9). */
public interface ServiceRepository extends JpaRepository<Service, Long> {}
