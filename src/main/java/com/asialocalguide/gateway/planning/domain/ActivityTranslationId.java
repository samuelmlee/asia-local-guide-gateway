package com.asialocalguide.gateway.planning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link ActivityTranslation}, comprising the activity ID and language ID.
 */
@Embeddable
@Getter
@NoArgsConstructor
public class ActivityTranslationId implements Serializable {

	@Column(name = "activity_id")
	private UUID activityId;

	@Column(name = "language_id")
	private Long languageId;

	/**
	 * @param activityId the UUID of the associated activity; must not be {@code null}
	 * @param languageId the ID of the associated language; must not be {@code null}
	 * @throws IllegalArgumentException if either argument is {@code null}
	 */
	public ActivityTranslationId(UUID activityId, Long languageId) {
		if (activityId == null || languageId == null) {
			throw new IllegalArgumentException(
					String.format("Activity ID: %s or Language ID: %s cannot be null", activityId, languageId));
		}
		this.activityId = activityId;
		this.languageId = languageId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		ActivityTranslationId that = (ActivityTranslationId) o;
		return Objects.equals(getActivityId(), that.getActivityId())
				&& Objects.equals(getLanguageId(), that.getLanguageId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getActivityId(), getLanguageId());
	}
}
