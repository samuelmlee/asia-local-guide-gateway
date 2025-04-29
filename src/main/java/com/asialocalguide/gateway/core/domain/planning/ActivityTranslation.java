package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
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

  @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
  @MapsId("activityId")
  @JoinColumn(name = "activity_id", nullable = false)
  private Activity activity;

  @NotEmpty private String title;

  private String description;

  public ActivityTranslation(LanguageCode languageCode, String title, String description) {
    if (languageCode == null || title == null) {
      throw new IllegalArgumentException("Language code and title cannot be null");
    }
    this.id = new ActivityTranslationId(languageCode);
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
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
