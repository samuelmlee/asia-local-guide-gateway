package com.asialocalguide.gateway.destination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link CountryTranslation}, combining a country ID and a language ID.
 */
@NoArgsConstructor
@Embeddable
@Getter
public class CountryTranslationId implements Serializable {
	@Column(name = "country_id")
	private Long countryId;

	@Column(name = "language_id")
	private Long languageId;

	/**
	 * @param countryId  the ID of the country; must not be {@code null}
	 * @param languageId the ID of the language; must not be {@code null}
	 * @throws IllegalArgumentException if either argument is {@code null}
	 */
	public CountryTranslationId(Long countryId, Long languageId) {
		if (countryId == null || languageId == null) {
			throw new IllegalArgumentException(
					String.format("Country ID: %s or Language ID: %s cannot be null", countryId, languageId));
		}
		this.countryId = countryId;
		this.languageId = languageId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CountryTranslationId that = (CountryTranslationId) o;
		return Objects.equals(getCountryId(), that.getCountryId())
				&& Objects.equals(getLanguageId(), that.getLanguageId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCountryId(), getLanguageId());
	}

	@Override
	public String toString() {
		return "CountryTranslationId{" + "countryId=" + getCountryId() + ", languageId=" + getLanguageId() + '}';
	}
}
