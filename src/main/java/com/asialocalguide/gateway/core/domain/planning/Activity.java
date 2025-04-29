package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@NoArgsConstructor
public class Activity {
  // Price and availability of an activity are fetched from the provider on demand

  @EmbeddedId @NotNull private ActivityId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookingProviderId")
  @JoinColumn(name = "booking_provider_id", nullable = false)
  @NotNull
  @Getter
  private BookingProvider provider;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private List<ActivityTranslation> activityTranslations;

  @DecimalMin(value = "0.0")
  @DecimalMax(value = "5.0")
  private Double averageRating;

  @Min(value = 0)
  private Integer reviewCount;

  @Min(value = 1)
  private Integer durationMinutes;

  @Embedded @NotNull private List<ActivityImage> coverImages;

  @NotBlank @URL private String bookingUrl;

  @LastModifiedDate private Instant lastUpdated;

  public Activity(
      ActivityId id,
      BookingProvider provider,
      List<ActivityTranslation> translations,
      Double averageRating,
      Integer reviewCount,
      Integer durationMinutes,
      List<ActivityImage> coverImages,
      String bookingUrl) {

    this.id = id;
    this.provider = provider;
    this.activityTranslations = translations;
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
    this.durationMinutes = durationMinutes;
    this.coverImages = coverImages;
    this.bookingUrl = bookingUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Activity activity = (Activity) o;
    return Objects.equals(id, activity.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
