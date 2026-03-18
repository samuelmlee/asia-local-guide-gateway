package com.asialocalguide.gateway.destination.domain;

import java.util.Optional;

public interface Translatable {

	Optional<String> getTranslation(LanguageCode languageCode);
}
