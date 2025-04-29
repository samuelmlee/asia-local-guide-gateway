package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.destination.LanguageCodeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class ActivityTranslationId implements Serializable {

  @Column(name = "activity_id")
  private Long activityId;

  @Column(name = "language_code")
  @Convert(converter = LanguageCodeConverter.class)
  private LanguageCode languageCode;

  public ActivityTranslationId(LanguageCode languageCode) {
    // activityId is set by Hibernate with @MapsId in ActivityTranslation
    this.languageCode = languageCode;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTranslationId that = (ActivityTranslationId) o;
    return Objects.equals(activityId, that.activityId) && languageCode == that.languageCode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(activityId, languageCode);
  }
}
