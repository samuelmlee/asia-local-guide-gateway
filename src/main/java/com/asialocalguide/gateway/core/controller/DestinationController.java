package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.service.DestinationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/destinations")
public class DestinationController {

  private final DestinationService destinationService;

  public DestinationController(DestinationService destinationService) {
    this.destinationService = destinationService;
  }

  @PostMapping("/sync/viator")
  public void syncDestinations() {
    destinationService.syncViatorDestinations();
  }
}
