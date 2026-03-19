package com.asialocalguide.gateway.planning.domain;

import com.asialocalguide.gateway.core.domain.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity holding the localized title and description for an {@link Activity}.
 *
 * <p>The composite key {@link ActivityTranslationId} combines the activity ID and language ID.
 * Constructing an instance automatically registers it with the parent activity via
 * {@link Activity#addTranslation(ActivityTranslation)}.
 */
@Getter
@Entity
@NoArgsConstructor
public class ActivityTranslation {

	@EmbeddedId
	private ActivityTranslationId id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "activity_id", insertable = false, updatable = false)
	private Activity activity;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", insertable = false, updatable = false)
	private Language language;

	@NotEmpty
	private String title;

	private String description;

	/**
	 * Creates a translation and registers it with the parent activity.
	 *
	 * @param activity    the owning activity; must not be {@code null}
	 * @param language    the language of this translation; must not be {@code null}
	 * @param title       the localized title; must not be {@code null}
	 * @param description the localized description; may be {@code null}
	 * @throws IllegalArgumentException if {@code activity}, {@code language}, or {@code title} is {@code null}
	 */
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
		if (o == null || getClass() != o.getClass())
			return false;
		ActivityTranslation that = (ActivityTranslation) o;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}
}
