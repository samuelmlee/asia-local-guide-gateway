package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ViatorDestinationService {

  private final ViatorClient viatorClient;

  public ViatorDestinationService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<ViatorDestinationDTO> getDestinationDTOs(SupportedLocale defaultLocale) {

    return viatorClient.getAllDestinationsForLocale(defaultLocale.getCode());
  }
}
