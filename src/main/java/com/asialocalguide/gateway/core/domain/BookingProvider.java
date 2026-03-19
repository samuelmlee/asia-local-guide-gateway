package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/**
 * Represents an external booking provider (e.g. Viator) that the gateway integrates with.
 *
 * <p>Booking providers are referenced by destination and activity tag provider mappings
 * to translate internal entities into provider-specific identifiers.
 */
@Entity
@Getter
@NoArgsConstructor
public class BookingProvider {

	@Id
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(unique = true, nullable = false)
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private BookingProviderName name;

	/**
	 * @param id   the database identifier; must not be {@code null}
	 * @param name the provider name; must not be {@code null}
	 * @throws IllegalArgumentException if either argument is {@code null}
	 */
	public BookingProvider(Long id, BookingProviderName name) {
		if (id == null || name == null) {
			throw new IllegalArgumentException("ID or BookingProviderName cannot be null");
		}
		this.id = id;
		this.name = name;
	}
}
