package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorActivitySearchDTO(
    Filtering filtering, Sorting sorting, Pagination pagination, String currency) {

  public record Filtering(
      String destination, List<Integer> tags, String startDate, String endDate, Range rating) {}

  public record Range(Integer from, Integer to) {}

  public record Sorting(ViatorActivitySortingType sort, ViatorActivitySortingOrder order) {}

  public record Pagination(Integer start, Integer count) {}
}
