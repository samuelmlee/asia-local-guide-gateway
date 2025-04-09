package com.asialocalguide.gateway.core.domain.activitytag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Embeddable
@Getter
public class ActivityTagProviderMappingId implements Serializable {
  @Column(name = "activity_tag_id")
  private Long activityTagId;

  @Column(name = "booking_provider_id")
  private Long bookingProviderId;

  public ActivityTagProviderMappingId(Long activityTagId, Long bookingProviderId) {
    this.activityTagId = activityTagId;
    this.bookingProviderId = bookingProviderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTagProviderMappingId that = (ActivityTagProviderMappingId) o;
    return Objects.equals(activityTagId, that.activityTagId)
        && Objects.equals(bookingProviderId, that.bookingProviderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activityTagId, bookingProviderId);
  }
}
