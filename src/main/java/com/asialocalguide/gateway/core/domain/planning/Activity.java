package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@NoArgsConstructor
public class Activity {
  // Price and availability of an activity are fetched from the provider on demand

  @EmbeddedId @NotNull @Getter private ActivityId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookingProviderId")
  @JoinColumn(name = "booking_provider_id", nullable = false)
  @NotNull
  @Getter
  private BookingProvider provider;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<ActivityTranslation> activityTranslations = new HashSet<>();

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ActivityImage> coverImages = new HashSet<>();

  @DecimalMin(value = "0.0")
  @DecimalMax(value = "5.0")
  private Double averageRating;

  @Min(value = 0)
  private Integer reviewCount;

  @Min(value = 1)
  private Integer durationMinutes;

  @NotBlank @URL private String bookingUrl;

  @LastModifiedDate private Instant lastUpdated;

  public Activity(
      ActivityId id,
      BookingProvider provider,
      Double averageRating,
      Integer reviewCount,
      Integer durationMinutes,
      String bookingUrl) {

    this.id = id;
    this.provider = provider;
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
    this.durationMinutes = durationMinutes;
    this.bookingUrl = bookingUrl;
  }

  public void addTranslation(ActivityTranslation translation) {
    if (translation == null) {
      return;
    }
    translation.setActivity(this);
    activityTranslations.add(translation);
  }

  public void removeTranslation(ActivityTranslation translation) {
    if (translation == null) {
      return;
    }
    translation.setActivity(null);
    activityTranslations.remove(translation);
  }

  public void addImage(ActivityImage image) {
    if (image == null) {
      return;
    }
    image.setActivity(this);
    coverImages.add(image);
  }

  public void removeImage(ActivityImage image) {
    if (image == null) {
      return;
    }
    image.setActivity(null);
    coverImages.remove(image);
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
