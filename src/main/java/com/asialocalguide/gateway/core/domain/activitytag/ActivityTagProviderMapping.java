package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class ActivityTagProviderMapping {

  @EmbeddedId @Setter private ActivityTagProviderMappingId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("activityTagId")
  @JoinColumn(name = "activity_tag_id")
  @NotNull
  private ActivityTag activityTag;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookingProviderId")
  @JoinColumn(name = "booking_provider_id")
  @NotNull
  private BookingProvider provider;

  @Setter @NotNull @NotEmpty private String providerActivityTagId;

  public ActivityTagProviderMapping(ActivityTag activityTag, BookingProvider provider, String providerActivityTagId) {
    if (activityTag == null) {
      throw new IllegalArgumentException("ActivityTag cannot be null");
    }
    if (provider == null) {
      throw new IllegalArgumentException("BookingProvider cannot be null");
    }

    this.id = new ActivityTagProviderMappingId();
    this.activityTag = activityTag;
    this.provider = provider;
    this.providerActivityTagId = providerActivityTagId;
  }

  void setActivityTag(ActivityTag activityTag) {
    this.activityTag = activityTag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ActivityTagProviderMapping that = (ActivityTagProviderMapping) o;

    if (id != null && that.id != null) {
      return id.equals(that.id);
    }

    return Objects.equals(provider, that.provider) && Objects.equals(providerActivityTagId, that.providerActivityTagId);
  }

  @Override
  public int hashCode() {
    if (id != null) {
      return id.hashCode();
    }

    return Objects.hash(provider, providerActivityTagId);
  }
}
