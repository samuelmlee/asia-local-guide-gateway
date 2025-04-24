package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorActivityDetailDTO(
    String productCode,
    String language,
    String title,
    PricingInfoDTO pricingInfo,
    List<ImageDTO> images,
    String description,
    List<Integer> tags,
    List<DestinationDTO> destinations,
    String productUrl,
    ReviewsDTO reviews) {

  public record PricingInfoDTO(String type, List<AgeBandDTO> ageBands) {}

  public record AgeBandDTO(
      String ageBand, int startAge, int endAge, int minTravelersPerBooking, int maxTravelersPerBooking) {}

  public record ImageDTO(String imageSource, String caption, boolean isCover, List<VariantDTO> variants) {}

  public record VariantDTO(int height, int width, String url) {}

  public record DestinationDTO(String ref, boolean primary) {}

  public record ReviewsDTO(
      List<SourceDTO> sources,
      List<ReviewCountTotalDTO> reviewCountTotals,
      int totalReviews,
      int combinedAverageRating) {}

  public record SourceDTO(String provider, List<ReviewCountDTO> reviewCounts, int totalCount, int averageRating) {}

  public record ReviewCountDTO(int rating, int count) {}

  public record ReviewCountTotalDTO(int rating, int count) {}
}
