package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorActivityAvailabilityDTO(
    String productCode, List<BookableItem> bookableItems, String currency, Summary summary) {
  public record BookableItem(String productOptionCode, List<Season> seasons) {}

  public record Season(String startDate, String endDate, List<PricingRecord> pricingRecords) {}

  public record PricingRecord(List<String> daysOfWeek, List<TimedEntry> timedEntries) {}

  public record TimedEntry(String startTime, List<UnavailableDate> unavailableDates) {}

  public record UnavailableDate(String date, String reason) {}

  public record Summary(double fromPrice) {}
}
