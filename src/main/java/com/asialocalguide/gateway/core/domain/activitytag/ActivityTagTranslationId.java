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
public class ActivityTagTranslationId implements Serializable {
  @Column(name = "activity_tag_id")
  private Long activityTagId;

  @Column(name = "language_id")
  private Long languageId;

  public ActivityTagTranslationId(Long activityTagId, Long languageId) {
    if (activityTagId == null || languageId == null) {
      throw new IllegalArgumentException(
          String.format("ActivityTag ID: %s or Language ID: %s cannot be null", activityTagId, languageId));
    }
    this.activityTagId = activityTagId;
    this.languageId = languageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTagTranslationId that = (ActivityTagTranslationId) o;
    return Objects.equals(getActivityTagId(), that.getActivityTagId())
        && Objects.equals(getLanguageId(), that.getLanguageId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getActivityTagId(), getLanguageId());
  }
}
