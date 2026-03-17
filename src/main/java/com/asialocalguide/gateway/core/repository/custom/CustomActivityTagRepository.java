package com.asialocalguide.gateway.core.repository.custom;

import java.util.List;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;

public interface CustomActivityTagRepository {

	List<ActivityTag> findAllWithTranslations(LanguageCode languageCode);
}
