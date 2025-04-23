package com.asialocalguide.gateway.core.domain.planning;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ActivityId implements Serializable {
  @Column(name = "provider_activity_id")
  private String providerActivityId;

  @Column(name = "booking_provider_id")
  private Long bookingProviderId;

  public ActivityId(String providerActivityId) {
    // bookingProviderId is set by @MapsId in Activity
    this.providerActivityId = providerActivityId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ActivityId that = (ActivityId) o;
    return Objects.equals(providerActivityId, that.providerActivityId)
        && Objects.equals(bookingProviderId, that.bookingProviderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(providerActivityId, bookingProviderId);
  }
}
