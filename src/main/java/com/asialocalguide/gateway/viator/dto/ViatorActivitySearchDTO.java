package com.asialocalguide.gateway.viator.dto;

import java.time.LocalDate;
import java.util.List;

public record ViatorActivitySearchDTO(
    Filtering filtering, Sorting sorting, Pagination pagination, String currency) {

  public record Filtering(
      Long destination, List<Long> tags, LocalDate startDate, LocalDate endDate, Range rating) {}

  public record Range(Integer from, Integer to) {}

  public record Sorting(ViatorActivitySortingType sort, ViatorActivitySortingOrder order) {}

  public record Pagination(Integer start, Integer count) {}
}
