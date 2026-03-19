package com.asialocalguide.gateway.destination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key for {@link DestinationTranslation}, combining a destination UUID
 * and a language ID.
 */
@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class DestinationTranslationId implements Serializable {
	@Column(name = "destination_id")
	private UUID destinationId;

	@Column(name = "language_id")
	private Long languageId;

	/**
	 * @param destinationId the UUID of the destination; must not be {@code null}
	 * @param languageId    the ID of the language; must not be {@code null}
	 * @throws IllegalArgumentException if either argument is {@code null}
	 */
	public DestinationTranslationId(UUID destinationId, Long languageId) {
		if (destinationId == null || languageId == null) {
			throw new IllegalArgumentException(
					String.format("DestinationId: %s or languageId: %s cannot be null", destinationId, languageId));
		}
		this.destinationId = destinationId;
		this.languageId = languageId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DestinationTranslationId that = (DestinationTranslationId) o;
		return Objects.equals(getDestinationId(), that.getDestinationId())
				&& Objects.equals(getLanguageId(), that.getLanguageId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDestinationId(), getLanguageId());
	}
}
