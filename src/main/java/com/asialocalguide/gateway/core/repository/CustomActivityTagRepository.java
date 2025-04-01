package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.activitytag.ActivityTag;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;

import java.util.List;

public interface CustomActivityTagRepository {

    List<ActivityTag> findAllWithTranslations(LanguageCode languageCode);
}
