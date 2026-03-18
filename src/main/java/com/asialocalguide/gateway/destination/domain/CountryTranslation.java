package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.Language;
import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Stores the localized name of a {@link Country} for a specific language.
 *
 * <p>The composite key is formed by the country ID and the language ID,
 * represented by {@link CountryTranslationId}.
 */
@Entity
@NoArgsConstructor
public class CountryTranslation {

	@EmbeddedId
	@Getter
	private CountryTranslationId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "country_id", insertable = false, updatable = false)
	@Getter
	private Country country;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", insertable = false, updatable = false)
	@Getter
	private Language language;

	@Getter
	@NotEmpty
	private String name;

	/**
	 * @param country  the country this translation belongs to; must not be {@code null}
	 * @param language the language of the translation; must not be {@code null}
	 * @param name     the localized country name; must not be {@code null}
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public CountryTranslation(Country country, Language language, String name) {
		if (country == null || language == null || name == null) {
			throw new IllegalArgumentException(
					String.format("Country: %s, Language: %s or name: %s cannot be null", country, language, name));
		}

		this.id = new CountryTranslationId(country.getId(), language.getId());
		this.country = country;
		this.language = language;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		CountryTranslation that = (CountryTranslation) o;
		return Objects.equal(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "CountryTranslation{" + ", id='" + getId() + '\'' + ", name='" + getName() + '\'' + '}';
	}
}
