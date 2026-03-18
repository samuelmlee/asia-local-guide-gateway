package com.asialocalguide.gateway.activitytag.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.asialocalguide.gateway.destination.domain.LanguageCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ActivityTag {

	@Id
	@Getter
	private Long id;

	@OneToMany(mappedBy = "activityTag", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ActivityTagTranslation> activityTagTranslations = new HashSet<>();

	@OneToMany(mappedBy = "activityTag", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ActivityTagProviderMapping> activityTagProviderMappings = new HashSet<>();

	/*
	 * Method needs the Language entity in ActivityTagTranslation to be eagerly
	 * loaded from the repository method.
	 */
	public Optional<ActivityTagTranslation> getTranslation(LanguageCode languageCode) {
		if (languageCode == null || activityTagTranslations.isEmpty()) {
			return Optional.empty();
		}

		return activityTagTranslations.stream()
				.filter(t -> t.getId() != null && languageCode.equals(t.getLanguage().getCode()))
				.findFirst();
	}

	public void addTranslation(ActivityTagTranslation translation) {
		if (translation == null) {
			return;
		}
		translation.setActivityTag(this);
		activityTagTranslations.add(translation);
	}

	public void removeTranslation(ActivityTagTranslation translation) {
		if (translation == null) {
			return;
		}
		translation.setActivityTag(null);
		activityTagTranslations.remove(translation);
	}

	public void addProviderMapping(ActivityTagProviderMapping mapping) {
		if (mapping == null) {
			return;
		}
		activityTagProviderMappings.add(mapping);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ActivityTag that = (ActivityTag) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
