package com.asialocalguide.gateway.auxiliary.service;

import com.asialocalguide.gateway.auxiliary.dto.DestinationDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuxiliaryService {

  @Autowired private RestClient auxiliaryClient;

  public List<DestinationDTO> getAllDestinations() {
    return null;
  }
}
