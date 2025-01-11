package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorActivityDTO(
    String productCode,
    String title,
    String description,
    List<ImageDTO> images,
    ReviewsDTO reviews,
    DurationDTO duration,
    String confirmationType,
    String itineraryType,
    PricingDTO pricing,
    String productUrl,
    List<DestinationDTO> destinations,
    List<Integer> tags,
    List<String> flags,
    TranslationInfoDTO translationInfo) {

  public record ImageDTO(
      String imageSource, String caption, boolean isCover, List<VariantDTO> variants) {
    public record VariantDTO(Integer height, Integer width, String url) {}
  }

  public record ReviewsDTO(
      List<SourceDTO> sources, Integer totalReviews, Double combinedAverageRating) {
    public record SourceDTO(String provider, Integer totalCount, Double averageRating) {}
  }

  public record DurationDTO(Integer fixedDurationInMinutes) {}

  public record PricingDTO(SummaryDTO summary, String currency) {
    public record SummaryDTO(Double fromPrice, Double fromPriceBeforeDiscount) {}
  }

  public record DestinationDTO(String ref, Boolean primary) {}

  public record TranslationInfoDTO(
      Boolean containsMachineTranslatedText, String translationSource) {}
}
