package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.viator.client.ViatorClient;
import org.springframework.stereotype.Service;

@Service
public class ViatorActivityService {

  private final ViatorClient viatorClient;

  public ViatorActivityService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }
}
