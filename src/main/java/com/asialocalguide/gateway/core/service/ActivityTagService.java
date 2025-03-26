package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.activitytag.ActivityTag;
import com.asialocalguide.gateway.core.domain.activitytag.ActivityTagTranslation;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.ActivityTagDTO;
import com.asialocalguide.gateway.core.repository.ActivityTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
public class ActivityTagService {

    private final ActivityTagRepository activityTagRepository;

    public ActivityTagService(ActivityTagRepository activityTagRepository) {
        this.activityTagRepository = activityTagRepository;
    }

    public List<ActivityTagDTO> getActivityTags() {

        // The header Accept-Language should be present in the request
        Locale locale = LocaleContextHolder.getLocale();

        LanguageCode languageCode;
        try {
            languageCode = LanguageCode.from(locale.getLanguage());
        } catch (Exception e) {
            languageCode = LanguageCode.EN;
        }
        final LanguageCode finalLanguageCode = languageCode;

        List<ActivityTag> activityTags = this.activityTagRepository.findAll();

        return activityTags.stream()
                .map(
                        activityTag -> {
                            ActivityTagTranslation translation = activityTag.getTranslation(finalLanguageCode).orElse(null);

                            if (translation == null) {
                                return null;
                            }

                            return new ActivityTagDTO(activityTag.getId(), translation.getName(), translation.getPromptText());
                        })
                .filter(Objects::nonNull)
                .toList();
    }
}
