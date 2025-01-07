package com.asialocalguide.gateway.core.converter;

import com.asialocalguide.gateway.core.domain.DestinationTranslation;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ViatorDestinationToDestinationTranslationConverter
    implements Converter<ViatorDestinationDTO, DestinationTranslation> {
  @Override
  public DestinationTranslation convert(ViatorDestinationDTO source) {
    return null;
  }
}
