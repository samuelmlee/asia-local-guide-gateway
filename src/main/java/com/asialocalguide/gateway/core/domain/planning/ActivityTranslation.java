package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class ActivityTranslation {

  @EmbeddedId private ActivityTranslationId id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "activity_id", insertable = false, updatable = false)
  private Activity activity;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", insertable = false, updatable = false)
  private Language language;

  @NotEmpty private String title;

  private String description;

  public ActivityTranslation(Activity activity, Language language, String title, String description) {
    if (activity == null || language == null || title == null) {
      throw new IllegalArgumentException("Activity, Language or title cannot be null");
    }
    this.id = new ActivityTranslationId(activity.getId(), language.getId());
    this.activity = activity;
    this.language = language;
    this.title = title;
    this.description = description;

    this.activity.addTranslation(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ActivityTranslation that = (ActivityTranslation) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
