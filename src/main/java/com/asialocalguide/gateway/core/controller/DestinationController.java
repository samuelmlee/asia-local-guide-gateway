package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.service.DestinationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
  public List<DestinationDTO> getAllDestinations() {
    return destinationService.getAllDestinations();
  }

  @PostMapping("/sync")
  public void syncDestinations() {
    destinationService.getAndPersistAllDestinations();
  }
}
