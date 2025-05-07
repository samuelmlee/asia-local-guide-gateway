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
  @JoinColumn(name = "activity_tag_id")
  private ActivityTag activityTag;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id")
  private Language language;

  @NotEmpty private String name;

  @NotEmpty private String promptText;

  public ActivityTagTranslation(ActivityTag activityTag, Language language, String name, String promptText) {
    if (activityTag == null || language == null || name == null) {
      throw new IllegalArgumentException(
          String.format("ActivityTag: %s or Language: %s or name: %s cannot be null", activityTag, language, name));
    }

    this.id = new ActivityTagTranslationId(activityTag.getId(), language.getId());
    this.activityTag = activityTag;
    this.name = name;
    this.promptText = promptText;
  }

  protected void setActivityTag(ActivityTag activityTag) {
    this.activityTag = activityTag;
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
