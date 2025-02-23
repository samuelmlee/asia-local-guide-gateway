package com.asialocalguide.gateway.core.domain.destination;

import java.util.Optional;

public interface Translatable {

  Optional<String> getTranslation(LanguageCode languageCode);
}
