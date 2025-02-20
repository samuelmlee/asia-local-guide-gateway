package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguageCodeConverter implements AttributeConverter<LanguageCode, String> {

  @Override
  public String convertToDatabaseColumn(LanguageCode languageCode) {
    return languageCode.toDbValue();
  }

  @Override
  public LanguageCode convertToEntityAttribute(String dbData) {
    return LanguageCode.from(dbData);
  }
}
