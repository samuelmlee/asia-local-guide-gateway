package com.asialocalguide.gateway.core.domain.activitytag;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
public class ActivityTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @OneToMany(mappedBy = "activityTag", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActivityTagTranslation> activityTagTranslations = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "activity_tag_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<ActivityTagProviderMapping> activityTagProviderMappings = new HashSet<>();

    public Optional<ActivityTagTranslation> getTranslation(LanguageCode languageCode) {
        return activityTagTranslations.stream()
                .filter(t -> t.getId().getLanguageCode().equals(languageCode))
                .findFirst();
    }

    public void addTranslation(ActivityTagTranslation translation) {
        translation.setActivityTag(this);
        activityTagTranslations.add(translation);
    }

    public void removeTranslation(ActivityTagTranslation translation) {
        if (activityTagTranslations != null) {
            translation.setActivityTag(null);
            activityTagTranslations.remove(translation);
        }
    }

    public void addProviderMapping(ActivityTagProviderMapping mapping) {
        if (mapping == null) {
            return;
        }
        activityTagProviderMappings.add(mapping);
    }

    public void removeProviderMapping(ActivityTagProviderMapping mapping) {
        if (mapping == null) {
            return;
        }
        activityTagProviderMappings.remove(mapping);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityTag that = (ActivityTag) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
