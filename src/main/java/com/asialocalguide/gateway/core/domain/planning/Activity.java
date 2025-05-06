package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Activity extends BaseEntity {
  // Price and availability of an activity are fetched from the provider on demand

  @Column(name = "provider_activity_id", nullable = false)
  @Getter
  private String providerActivityId;

  @ManyToOne(fetch = FetchType.LAZY)
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
  private Float averageRating;

  @Min(value = 0)
  private Integer reviewCount;

  @Min(value = 1)
  private Integer durationMinutes;

  @NotBlank @URL private String bookingUrl;

  @LastModifiedDate private Instant lastUpdated;

  public Activity(
      String providerActivityId,
      BookingProvider provider,
      Float averageRating,
      Integer reviewCount,
      Integer durationMinutes,
      String bookingUrl) {

    this.providerActivityId = providerActivityId;
    this.provider = provider;
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
    this.durationMinutes = durationMinutes;
    this.bookingUrl = bookingUrl;
  }

  public Set<ActivityTranslation> getActivityTranslations() {
    return Collections.unmodifiableSet(activityTranslations);
  }

  public Set<ActivityImage> getCoverImages() {
    return Collections.unmodifiableSet(coverImages);
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
}
