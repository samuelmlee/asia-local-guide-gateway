package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/**
 * Represents a bookable destination (city, region, etc.) with localized names and
 * mappings to provider-specific destination identifiers.
 *
 * <p>Implements {@link Translatable} so callers can retrieve the localized name for any
 * supported language without knowing the internal translation structure.
 */
@Entity
@NoArgsConstructor
public class Destination extends BaseEntity implements Translatable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "country_id")
	@NotNull
	@Getter
	private Country country;

	@OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
	@NotEmpty
	private Set<DestinationTranslation> destinationTranslations = new HashSet<>();

	@Enumerated(EnumType.STRING)
	@NotNull
	@Getter
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private DestinationType type;

	@OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<DestinationProviderMapping> destinationProviderMappings = new HashSet<>();

	@NotNull
	@Embedded
	@Getter
	Coordinates centerCoordinates;

	/**
	 * @param country           the country this destination belongs to; must not be {@code null}
	 * @param type              the destination classification; must not be {@code null}
	 * @param centerCoordinates the geographic center of the destination; must not be {@code null}
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public Destination(Country country, DestinationType type, Coordinates centerCoordinates) {
		if (country == null || centerCoordinates == null || type == null) {
			throw new IllegalArgumentException(
					String.format("Country: %s, centerCoordinates: %s or type: %s cannot be null",
							country,
							centerCoordinates,
							type));
		}

		this.country = country;
		this.type = type;
		this.centerCoordinates = centerCoordinates;
	}

	@Override
	public Optional<String> getTranslation(LanguageCode languageCode) {
		if (languageCode == null || destinationTranslations.isEmpty()) {
			return Optional.empty();
		}

		return destinationTranslations.stream()
				.filter(t -> t.getId() != null && languageCode.equals(t.getLanguage().getCode()))
				.findFirst()
				.map(DestinationTranslation::getName);
	}

	/**
	 * Adds a localized name translation to this destination.
	 *
	 * @param translation the translation to add; ignored if {@code null}
	 */
	public void addTranslation(DestinationTranslation translation) {
		if (translation == null) {
			return;
		}
		destinationTranslations.add(translation);
	}

	/**
	 * Returns the number of translations currently associated with this destination.
	 *
	 * @return the translation count; {@code 0} if there are no translations
	 */
	public int getTranslationCount() {
		if (destinationTranslations == null) {
			return 0;
		}
		return destinationTranslations.size();
	}

	/**
	 * Adds a provider mapping that links this destination to a booking provider.
	 *
	 * @param mapping the provider mapping to add; ignored if {@code null}
	 */
	public void addProviderMapping(DestinationProviderMapping mapping) {
		if (mapping == null) {
			return;
		}
		destinationProviderMappings.add(mapping);
	}

	/**
	 * Returns the provider mapping for the given booking provider ID, if one exists.
	 *
	 * @param providerId the database ID of the booking provider; returns empty if {@code null}
	 * @return an Optional containing the matching mapping, or empty if not found
	 */
	public Optional<DestinationProviderMapping> getBookingProviderMapping(Long providerId) {
		if (providerId == null || destinationProviderMappings.isEmpty()) {
			return Optional.empty();
		}

		return destinationProviderMappings.stream()
				.filter(mapping -> mapping.getProvider() != null && mapping.getProvider().getId() != null
						&& providerId.equals(mapping.getProvider().getId()))
				.findFirst();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Destination that = (Destination) o;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return "Destination{" + "id=" + getId() + ", type=" + type + '}';
	}
}
