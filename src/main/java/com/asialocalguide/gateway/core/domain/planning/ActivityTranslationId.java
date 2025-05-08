package com.asialocalguide.gateway.core.domain.planning;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ActivityTranslationId implements Serializable {

  @Column(name = "activity_id")
  private UUID activityId;

  @Column(name = "language_id")
  private Long languageId;

  public ActivityTranslationId(UUID activityId, Long languageId) {
    if (activityId == null || languageId == null) {
      throw new IllegalArgumentException(
          String.format("Activity ID: %s or Language ID: %s cannot be null", activityId, languageId));
    }
    this.activityId = activityId;
    this.languageId = languageId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTranslationId that = (ActivityTranslationId) o;
    return Objects.equals(getActivityId(), that.getActivityId())
        && Objects.equals(getLanguageId(), that.getLanguageId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getActivityId(), getLanguageId());
  }
}
