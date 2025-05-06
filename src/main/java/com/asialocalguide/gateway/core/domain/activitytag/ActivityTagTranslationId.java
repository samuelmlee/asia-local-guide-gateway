package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
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

  public ActivityTagTranslationId(LanguageCode languageCode) {
    // activityTagId and languageId set by Hibernate with @MapsId in ActivityTagTranslation
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTagTranslationId that = (ActivityTagTranslationId) o;
    return Objects.equals(activityTagId, that.activityTagId) && Objects.equals(languageId, that.languageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activityTagId, languageId);
  }
}
