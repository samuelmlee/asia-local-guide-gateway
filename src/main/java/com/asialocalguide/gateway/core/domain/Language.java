package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.asialocalguide.gateway.destination.domain.LanguageCode;

/**
 * Represents a language supported by the application for content translation.
 *
 * <p>Referenced by translation entities (e.g. {@code ActivityTagTranslation}) to
 * associate localised text with a specific {@link LanguageCode}.
 */
@Entity
@NoArgsConstructor
@Getter
public class Language {

	@Id
	private Long id;

	@Column(nullable = false, unique = true, length = 2)
	@Enumerated(EnumType.STRING)
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private LanguageCode code;

	/**
	 * @param id   the database identifier; must not be {@code null}
	 * @param code the ISO language code; must not be {@code null}
	 * @throws IllegalArgumentException if either argument is {@code null}
	 */
	public Language(Long id, LanguageCode code) {
		if (id == null || code == null) {
			throw new IllegalArgumentException("Id and code cannot be null");
		}
		this.id = id;
		this.code = code;
	}
}
