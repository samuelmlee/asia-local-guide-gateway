package com.asialocalguide.gateway.destination.domain;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * Represents a country identified by its ISO 3166-1 alpha-2 code.
 *
 * <p>Countries own a collection of {@link CountryTranslation} entries providing
 * localised names and implement {@link Translatable} for uniform name resolution.
 */
@Entity
@NoArgsConstructor
public class Country implements Translatable {

	@Id
	@Getter
	private Long id;

	@OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CountryTranslation> countryTranslations = new HashSet<>();

	@Column(name = "iso_2_code")
	@NotEmpty
	@Length(min = 2, max = 2)
	@Getter
	@Setter
	private String iso2Code;

	/**
	 * @param iso2Code the ISO 3166-1 alpha-2 country code; must be exactly 2 characters and not {@code null}
	 * @throws IllegalArgumentException if the code is {@code null} or not exactly 2 characters long
	 */
	public Country(String iso2Code) {
		if (iso2Code == null || iso2Code.length() != 2) {
			throw new IllegalArgumentException("ISO2 code must be 2 characters long");
		}

		this.iso2Code = iso2Code;
	}

	/**
	 * Adds a localised name translation to this country.
	 *
	 * @param translation the translation to add; must not be {@code null}
	 * @throws IllegalArgumentException if {@code translation} is {@code null}
	 */
	public void addTranslation(CountryTranslation translation) {
		if (translation == null) {
			throw new IllegalArgumentException("Translation cannot be null");
		}
		if (countryTranslations == null) {
			countryTranslations = new HashSet<>();
		}
		countryTranslations.add(translation);
	}

	/*
	 * Method needs the Language entity in CountryTranslation to be eagerly loaded.
	 */
	/**
	 * {@inheritDoc}
	 *
	 * <p>Requires the {@link com.asialocalguide.gateway.core.domain.Language} entity inside
	 * each {@link CountryTranslation} to be eagerly loaded to avoid lazy-loading issues.
	 */
	@Override
	public Optional<String> getTranslation(LanguageCode languageCode) {
		if (languageCode == null || countryTranslations == null) {
			return Optional.empty();
		}

		return countryTranslations.stream()
				.filter(ct -> ct.getId() != null && languageCode.equals(ct.getLanguage().getCode()))
				.findFirst()
				.map(CountryTranslation::getName);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		Country country = (Country) o;
		return Objects.equal(getId(), country.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}
}
