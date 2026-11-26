package com.smartappointment.common.web;

import org.springframework.data.domain.Page;

/** Paging metadata of a list response (contract: PageInfo; page numbers start at 0). */
public record PageMetadata(int page, int size, long totalElements, int totalPages) {

  public static PageMetadata from(Page<?> page) {
    return new PageMetadata(
        page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }
}
