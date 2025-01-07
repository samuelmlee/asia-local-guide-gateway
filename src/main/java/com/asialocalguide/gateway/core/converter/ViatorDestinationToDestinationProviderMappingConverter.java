package com.asialocalguide.gateway.core.converter;

import com.asialocalguide.gateway.core.domain.DestinationProviderMapping;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ViatorDestinationToDestinationProviderMappingConverter
    implements Converter<ViatorDestinationDTO, DestinationProviderMapping> {

  @Override
  public DestinationProviderMapping convert(ViatorDestinationDTO dto) {

    return DestinationProviderMapping.builder()
        .providerDestinationId(String.valueOf(dto.getDestinationId()))
        .build();
  }
}
