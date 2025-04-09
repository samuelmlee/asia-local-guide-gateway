package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.destination.LanguageCodeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Embeddable
@Getter
public class ActivityTagTranslationId {
  @Column(name = "activity_tag_id")
  private Long activityTagId;

  @Column(name = "language_code")
  @Convert(converter = LanguageCodeConverter.class)
  private LanguageCode languageCode;

  public ActivityTagTranslationId(LanguageCode languageCode) {
    // activityTagId is set by Hibernate with @MapsId in ActivityTagTranslation
    this.languageCode = languageCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTagTranslationId that = (ActivityTagTranslationId) o;
    return Objects.equals(activityTagId, that.activityTagId) && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activityTagId, languageCode);
  }
}
