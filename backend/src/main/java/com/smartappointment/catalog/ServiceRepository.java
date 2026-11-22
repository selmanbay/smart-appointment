package com.smartappointment.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for {@link Service} (SRS Table 9). */
public interface ServiceRepository extends JpaRepository<Service, Long> {

  List<Service> findByOrganizationIdAndIsActiveTrue(Long organizationId);
}
