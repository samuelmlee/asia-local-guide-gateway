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
  @MapsId("activityId")
  @JoinColumn(name = "activity_id", nullable = false)
  private Activity activity;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @MapsId("languageId")
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  @NotEmpty private String title;

  private String description;

  public ActivityTranslation(Language language, String title, String description) {
    if (language == null || title == null) {
      throw new IllegalArgumentException("Language and title cannot be null");
    }
    this.id = new ActivityTranslationId();
    this.language = language;
    this.title = title;
    this.description = description;
  }

  void setActivity(Activity activity) {
    this.activity = activity;
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
