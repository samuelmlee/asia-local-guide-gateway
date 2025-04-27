package com.asialocalguide.gateway.viator.dto;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record ViatorActivityDetailDTO(
    String productCode,
    String language,
    String title,
    String description,
    List<ImageDTO> images,
    List<Integer> tags,
    List<DestinationDTO> destinations,
    ItineraryDTO itinerary,
    String productUrl,
    ReviewsDTO reviews) {

  // Only cover image need to be persisted
  public record ImageDTO(String imageSource, String caption, boolean isCover, List<ImageVariantDTO> variants) {}

  public record ImageVariantDTO(int height, int width, String url) {}

  public record DestinationDTO(String ref, boolean primary) {}

  public record ItineraryDTO(String itineraryType, DurationDTO duration) {}

  public record DurationDTO(
      Integer variableDurationFromMinutes, Integer variableDurationToMinutes, Integer fixedDurationInMinutes) {}

  public record ReviewsDTO(
      List<ReviewCountTotalDTO> reviewCountTotals, int totalReviews, double combinedAverageRating) {}

  public record ReviewCountTotalDTO(int rating, int count) {}

  public int getDurationMinutes() {

    if (itinerary() == null || itinerary().duration() == null) {
      return 0;
    }

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
