package com.asialocalguide.gateway.viator.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.hibernate.validator.constraints.URL;

public record ViatorActivityDetailDTO(
    @NotBlank String productCode,
    @NotBlank String language,
    @NotBlank String title,
    @NotBlank String description,
    @NotEmpty List<ImageDTO> images,
    List<Integer> tags,
    List<DestinationDTO> destinations,
    @NotNull ItineraryDTO itinerary,
    @URL String productUrl,
    @NotNull ReviewsDTO reviews) {

  // Only cover image need to be persisted
  public record ImageDTO(String imageSource, String caption, boolean isCover, List<ImageVariantDTO> variants) {}

  public record ImageVariantDTO(@Positive int height, @Positive int width, @URL String url) {}

  public record DestinationDTO(String ref, boolean primary) {}

  public record ItineraryDTO(String itineraryType, @NotNull DurationDTO duration) {}

  public record DurationDTO(
      Integer variableDurationFromMinutes, Integer variableDurationToMinutes, Integer fixedDurationInMinutes) {}

  public record ReviewsDTO(
      List<ReviewCountTotalDTO> reviewCountTotals,
      @Positive int totalReviews,
      @DecimalMin("0.0") @DecimalMax("5.0") float combinedAverageRating) {}

  public record ReviewCountTotalDTO(int rating, int count) {}

  public int getDurationMinutes() {

    ViatorActivityDetailDTO.DurationDTO duration = itinerary().duration();

    if (duration.fixedDurationInMinutes != null) {
      return duration.fixedDurationInMinutes();
    }

    if (duration.variableDurationToMinutes() != null) {
      return duration.variableDurationToMinutes();
    }

    return 0;
  }

  public Optional<ImageVariantDTO> getCoverImage(Predicate<ImageVariantDTO> imageFilter) {
    if (images() == null || images().isEmpty()) {
      return Optional.empty();
    }

    return images().stream()
        .filter(image -> image.isCover() && image.variants() != null && !image.variants().isEmpty())
        .findFirst()
        .flatMap(image -> image.variants().stream().filter(imageFilter).findFirst());
  }
}
