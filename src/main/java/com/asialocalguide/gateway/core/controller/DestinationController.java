package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.service.DestinationService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/destinations")
public class DestinationController {

  private final DestinationService destinationService;

  public DestinationController(DestinationService destinationService) {
    this.destinationService = destinationService;
  }

  @GetMapping("/autocomplete")
  public List<DestinationDTO> getAutocompleteSuggestions(@RequestParam String query) {
    return destinationService.getAutocompleteSuggestions(query);
  }

  @PostMapping("/sync/viator")
  public void syncViatorDestinations() {
    destinationService.syncViatorDestinations();
  }
}
