package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class ActivityTagProviderMapping {

  @EmbeddedId @Setter private ActivityTagProviderMappingId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activity_tag_id", insertable = false, updatable = false)
  @NotNull
  private ActivityTag activityTag;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_provider_id", insertable = false, updatable = false)
  @NotNull
  private BookingProvider provider;

  @Setter @NotNull @NotEmpty private String providerActivityTagId;

  public ActivityTagProviderMapping(ActivityTag activityTag, BookingProvider provider, String providerActivityTagId) {
    if (activityTag == null || provider == null || providerActivityTagId == null) {
      throw new IllegalArgumentException(
          String.format(
              "ActivityTag: %s, BookingProvider: %s or providerActivityTagId: %s cannot be null",
              activityTag, provider, providerActivityTagId));
    }

    this.id = new ActivityTagProviderMappingId(activityTag.getId(), provider.getId());
    this.activityTag = activityTag;
    this.provider = provider;
    this.providerActivityTagId = providerActivityTagId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ActivityTagProviderMapping that)) return false;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
