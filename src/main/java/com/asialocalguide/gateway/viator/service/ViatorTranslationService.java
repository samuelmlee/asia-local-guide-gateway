package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.DestinationTranslation;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ViatorTranslationService {

  private final ViatorClient viatorClient;

  public ViatorTranslationService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public DestinationTranslation createTranslation(
      ViatorDestinationDTO viatorDestination, SupportedLocale supportedLocale) {
    return new DestinationTranslation(supportedLocale.getCode(), viatorDestination.getName());
  }

  private Map<Long, Set<DestinationTranslation>> buildAdditionalTranslationsMap() {

    Map<Long, Set<DestinationTranslation>> translationsMap = new HashMap<>();

    List<SupportedLocale> additionalLocales = getNonDefaultLocales();

    additionalLocales.forEach(
        locale -> {
          List<ViatorDestinationDTO> destinationDTOs =
              viatorClient.getAllDestinationsForLocale(locale.getCode());

          destinationDTOs.forEach(
              dto -> {
                Long destinationId = dto.getDestinationId();

                DestinationTranslation translation = createTranslation(dto, locale);

                Set<DestinationTranslation> translationList =
                    translationsMap.getOrDefault(destinationId, new HashSet<>());

                translationList.add(translation);

                translationsMap.put(destinationId, translationList);
              });
        });

    return translationsMap;
  }

  private static List<SupportedLocale> getNonDefaultLocales() {
    return SupportedLocale.stream().filter(locale -> !locale.isDefault()).toList();
  }
}
