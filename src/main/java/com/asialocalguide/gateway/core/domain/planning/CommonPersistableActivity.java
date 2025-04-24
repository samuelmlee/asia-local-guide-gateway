package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CommonPersistableActivity(
    @NotNull @NotEmpty List<Translation> title,
    @NotNull @NotEmpty List<Translation> description,
    List<Image> images,
    Review review,
    Integer durationInMinutes,
    Pricing pricing,
    String providerUrl,
    List<String> categories,
    BookingProviderName providerName,
    String providerId) {

  public record Image(ImageType type, Integer height, Integer width, String url) {}

  public record Review(Double averageRating, Integer reviewCount) {}

  public record Pricing(Double price, String currency) {}

  public record Translation(LanguageCode languageCode, String value) {}

  public enum ImageType {
    MOBILE,
    DESKTOP
  }
}
