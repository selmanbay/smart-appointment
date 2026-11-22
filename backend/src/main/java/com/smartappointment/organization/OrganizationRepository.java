package com.smartappointment.organization;

import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Organization} (SRS Table 9). */
public interface OrganizationRepository extends JpaRepository<Organization, Long> {}
