package com.asialocalguide.gateway.activitytag.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.activitytag.domain.ActivityTagTranslation;
import com.asialocalguide.gateway.activitytag.dto.ActivityTagDTO;
import com.asialocalguide.gateway.activitytag.repository.ActivityTagRepository;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for retrieving activity tags with localized translations.
 *
 * <p>Resolves the current locale from the {@code Accept-Language} request header
 * and maps {@link com.asialocalguide.gateway.activitytag.domain.ActivityTag} entities
 * to {@link ActivityTagDTO} instances using the matching translation.
 */
@Service
@Slf4j
public class ActivityTagService {

	private final ActivityTagRepository activityTagRepository;

	/**
	 * @param activityTagRepository repository used to fetch activity tags with their translations
	 */
	public ActivityTagService(ActivityTagRepository activityTagRepository) {
		this.activityTagRepository = activityTagRepository;
	}

	/**
	 * Returns all activity tags translated into the language resolved from the current request locale.
	 *
	 * <p>Falls back to {@link LanguageCode#EN} when the request locale cannot be mapped to a
	 * supported language. Tags without a matching translation are excluded from the result.
	 *
	 * @return list of localized activity tag DTOs; never {@code null}
	 */
	public List<ActivityTagDTO> getActivityTags() {

		// The header Accept-Language should be present in the request
		Locale locale = LocaleContextHolder.getLocale();

		LanguageCode languageCode = LanguageCode.from(locale.getLanguage()).orElse(LanguageCode.EN);

		List<ActivityTag> activityTags = this.activityTagRepository.findAllWithTranslations(languageCode);

		return activityTags.stream().map(activityTag -> {
			ActivityTagTranslation translation = activityTag.getTranslation(languageCode).orElse(null);

			if (translation == null) {
				return null;
			}

			return new ActivityTagDTO(activityTag.getId(), translation.getName(), translation.getPromptText());
		}).filter(Objects::nonNull).toList();
	}
}
