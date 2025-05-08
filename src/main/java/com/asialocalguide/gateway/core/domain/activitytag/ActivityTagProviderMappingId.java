package com.asialocalguide.gateway.core.domain.activitytag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;

@Embeddable
@Getter
public class ActivityTagProviderMappingId implements Serializable {
  @Column(name = "activity_tag_id")
  private Long activityTagId;

  @Column(name = "booking_provider_id")
  private Long bookingProviderId;

  public ActivityTagProviderMappingId(Long activityTagId, Long bookingProviderId) {
    if (activityTagId == null || bookingProviderId == null) {
      throw new IllegalArgumentException(
          String.format(
              "ActivityTag ID: %s or Booking Provider ID: %s cannot be null", activityTagId, bookingProviderId));
    }
    this.activityTagId = activityTagId;
    this.bookingProviderId = bookingProviderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTagProviderMappingId that = (ActivityTagProviderMappingId) o;
    return Objects.equals(getActivityTagId(), that.getActivityTagId())
        && Objects.equals(getBookingProviderId(), that.getBookingProviderId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getActivityTagId(), getBookingProviderId());
  }
}
