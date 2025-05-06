package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ActivityTagTranslation {

  @EmbeddedId private ActivityTagTranslationId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("activityTagId")
  @JoinColumn(name = "activity_tag_id")
  private ActivityTag activityTag;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("languageId")
  @JoinColumn(name = "language_id")
  private Language language;

  @NotEmpty private String name;

  @NotEmpty private String promptText;

  public ActivityTagTranslation(ActivityTag activityTag, Language language, String name, String promptText) {
    if (activityTag == null) {
      throw new IllegalArgumentException("ActivityTag cannot be null");
    }
    if (language == null) {
      throw new IllegalArgumentException("LanguageCode cannot be null");
    }

    this.id = new ActivityTagTranslationId();
    this.activityTag = activityTag;
    this.name = name;
    this.promptText = promptText;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    ActivityTagTranslation that = (ActivityTagTranslation) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ActivityTagTranslation{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", promptText='"
        + promptText
        + '\''
        + '}';
  }
}
