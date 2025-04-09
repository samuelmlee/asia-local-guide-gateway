package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
public class ActivityTagTranslation {

  @EmbeddedId private ActivityTagTranslationId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("activityTagId")
  @JoinColumn(name = "activity_tag_id")
  @Setter
  private ActivityTag activityTag;

  @NotEmpty private String name;

  @NotEmpty private String promptText;

  public ActivityTagTranslation(ActivityTag activityTag, LanguageCode languageCode, String name, String promptText) {
    if (activityTag == null) {
      throw new IllegalArgumentException("ActivityTag cannot be null");
    }
    if (languageCode == null) {
      throw new IllegalArgumentException("LanguageCode cannot be null");
    }

    this.id = new ActivityTagTranslationId(languageCode);
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
        + ", languageCode='"
        + id.getLanguageCode()
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
