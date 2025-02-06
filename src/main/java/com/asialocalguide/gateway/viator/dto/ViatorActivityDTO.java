package com.asialocalguide.gateway.viator.dto;

import com.asialocalguide.gateway.core.dto.ImageDTO;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ViatorActivityDTO(
    String productCode,
    String title,
    String description,
    List<ImageDTO> images,
    @NotNull ReviewsDTO reviews,
    @NotNull DurationDTO duration,
    String confirmationType,
    String itineraryType,
    PricingDTO pricing,
    String productUrl,
    List<DestinationDTO> destinations,
    List<Integer> tags,
    List<String> flags,
    TranslationInfoDTO translationInfo) {

  public record ReviewsDTO(
      List<SourceDTO> sources,
      @NotNull Integer totalReviews,
      @NotNull Double combinedAverageRating) {
    public record SourceDTO(String provider, Integer totalCount, Double averageRating) {}
  }

  public record DurationDTO(
      Integer variableDurationFromMinutes,
      Integer variableDurationToMinutes,
      Integer fixedDurationInMinutes) {}

  public record PricingDTO(SummaryDTO summary, String currency) {
    public record SummaryDTO(Double fromPrice, Double fromPriceBeforeDiscount) {}
  }

  public record DestinationDTO(String ref, Boolean primary) {}

  public record TranslationInfoDTO(
      Boolean containsMachineTranslatedText, String translationSource) {}

  public int getDurationMinutes() {

    DurationDTO duration = duration();

    if (duration.fixedDurationInMinutes != null) {
      return duration.fixedDurationInMinutes();
    }

    if (duration.variableDurationToMinutes() != null) {
      return duration.variableDurationToMinutes();
    }

    return 0;
  }
}
