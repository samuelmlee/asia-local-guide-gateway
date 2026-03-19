package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.repository.LanguageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving {@link Language} entities.
 */
@Service
public class LanguageService {

	private final LanguageRepository languageRepository;

	/**
	 * @param languageRepository repository for language lookups
	 */
	public LanguageService(LanguageRepository languageRepository) {
		this.languageRepository = languageRepository;
	}

	/**
	 * Returns all languages persisted in the database.
	 *
	 * @return list of all {@link Language} entities; never {@code null}
	 */
	public List<Language> getAllLanguages() {
		return languageRepository.findAll();
	}
}
