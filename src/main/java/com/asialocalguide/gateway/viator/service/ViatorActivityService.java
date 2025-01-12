package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ViatorActivityService {

  private final ViatorClient viatorClient;

  public ViatorActivityService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<ViatorActivityDTO> getActivityDTOs(
      SupportedLocale defaultLocale, ViatorActivitySearchDTO searchDTO) {

    return viatorClient.getActivitiesByRequestAndLocale(defaultLocale.getCode(), searchDTO);
  }
}
