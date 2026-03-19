package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the localized name of a {@link Destination} for a specific language.
 *
 * <p>The composite key is formed by the destination UUID and the language ID,
 * represented by {@link DestinationTranslationId}.
 */
@Entity
@NoArgsConstructor
public class DestinationTranslation {

	@EmbeddedId
	@Getter
	private DestinationTranslationId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destination_id", insertable = false, updatable = false)
	@Getter
	private Destination destination;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", insertable = false, updatable = false)
	@Getter
	private Language language;

	@Setter
	@NotEmpty
	@Getter
	private String name;

	/**
	 * @param destination the destination this translation belongs to; must not be {@code null}
	 * @param language    the language of the translation; must not be {@code null}
	 * @param name        the localized destination name; must not be {@code null}
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public DestinationTranslation(Destination destination, Language language, String name) {
		if (destination == null || language == null || name == null) {
			throw new IllegalArgumentException(String
					.format("Destination: %s, language: %s or name: %s cannot be null", destination, language, name));
		}

		this.id = new DestinationTranslationId(destination.getId(), language.getId());
		this.destination = destination;
		this.language = language;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DestinationTranslation that = (DestinationTranslation) o;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return "DestinationTranslation{" + ", id='" + id + '\'' + ", name='" + name + '\'' + '}';
	}
}
