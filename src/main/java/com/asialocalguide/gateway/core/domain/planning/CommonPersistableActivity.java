package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import jakarta.validation.constraints.*;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record CommonPersistableActivity(
    @NotNull @NotEmpty List<Translation> title,
    @NotNull @NotEmpty List<Translation> description,
    List<Image> images,
    @NotNull Review review,
    @NotNull @Positive Integer durationInMinutes,
    @URL String providerUrl,
    @NotNull BookingProviderName providerName,
    @NotNull String providerId) {

  public record Image(
      @NotNull ImageType type,
      @NotNull @Positive Integer height,
      @NotNull @Positive Integer width,
      @NotBlank String url) {}

  public record Review(
      @NotNull @DecimalMin("0.0") @DecimalMax("5.0") Float averageRating, @NotNull @Positive Integer reviewCount) {}

  public record Translation(@NotNull LanguageCode languageCode, @NotBlank String value) {}
}
