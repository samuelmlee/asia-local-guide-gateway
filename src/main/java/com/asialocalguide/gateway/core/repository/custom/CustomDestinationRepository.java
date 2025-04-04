package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import java.util.List;

public interface CustomDestinationRepository {

  List<Destination> findCityOrRegionByNameWithEagerTranslations(LanguageCode languageCode, String name);
}
