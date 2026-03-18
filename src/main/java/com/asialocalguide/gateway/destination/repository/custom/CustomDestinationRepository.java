package com.asialocalguide.gateway.destination.repository.custom;

import java.util.List;

import com.asialocalguide.gateway.destination.domain.Destination;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

public interface CustomDestinationRepository {

	List<Destination> findCityOrRegionByNameWithEagerTranslations(LanguageCode languageCode, String name);
}
