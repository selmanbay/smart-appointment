package com.smartappointment.catalog;

import com.smartappointment.catalog.dto.ServiceResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contract operation listServices. */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/services")
public class CatalogController {

  private final CatalogService catalogService;

  public CatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping
  public List<ServiceResponse> listServices(@PathVariable Long organizationId) {
    return catalogService.listActiveServices(organizationId);
  }
}
