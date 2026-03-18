package com.asialocalguide.gateway.activitytag.repository.custom;

import java.util.List;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

public interface CustomActivityTagRepository {

	List<ActivityTag> findAllWithTranslations(LanguageCode languageCode);
}
