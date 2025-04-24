package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Activity {

  @EmbeddedId @NotNull private ActivityId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookingProviderId")
  @JoinColumn(name = "booking_provider_id", nullable = false)
  @NotNull
  @Getter
  private BookingProvider provider;

  @NotBlank private String title;

  @NotBlank private String description;

  @DecimalMin(value = "0.0")
  @DecimalMax(value = "5.0")
  private Double averageRating;

  @Min(value = 0)
  private Integer reviewCount;

  @Min(value = 1)
  private Integer durationMinutes;

  @NotBlank private String currency;

  @Embedded @NotNull private ActivityImage mainImage;

  @NotBlank @URL private String bookingUrl;

  @NotNull private LocalDateTime lastUpdated;

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
