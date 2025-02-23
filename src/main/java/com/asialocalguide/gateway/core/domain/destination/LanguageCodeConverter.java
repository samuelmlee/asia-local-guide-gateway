package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Objects;

@Converter(autoApply = true)
public class LanguageCodeConverter implements AttributeConverter<LanguageCode, String> {

  @Override
  public String convertToDatabaseColumn(LanguageCode languageCode) {
    Objects.requireNonNull(languageCode);

    return languageCode.toDbValue();
  }

  @Override
  public LanguageCode convertToEntityAttribute(String dbData) {
    Objects.requireNonNull(dbData);

    return LanguageCode.from(dbData);
  }
}
