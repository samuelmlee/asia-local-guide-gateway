package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.activity.domain.ActivityTag;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import java.util.List;

public interface CustomActivityTagRepository {

	List<ActivityTag> findAllWithTranslations(LanguageCode languageCode);
}
