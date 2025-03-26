package com.asialocalguide.gateway.core.domain.activitytag;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
public class ActivityTagTranslation {

    @EmbeddedId
    private ActivityTagTranslationId id;

    @ManyToOne
    @MapsId("activityTagId")
    @JoinColumn(name = "activity_tag_id")
    @Setter
    private ActivityTag activityTag;

    @NotEmpty
    private String name;

    @NotEmpty
    private String promptText;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ActivityTagTranslation that = (ActivityTagTranslation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ActivityTagTranslation{" + ", languageCode='" + id.getLanguageCode() + '\'' + ", name='" + name + '\'' + '}';
    }
}
