package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.service.destination.DestinationService;
import jakarta.validation.constraints.Size;
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
  public List<DestinationDTO> getAutocompleteSuggestions(@RequestParam(required = true) @Size(min = 1) String query) {
    return destinationService.getAutocompleteSuggestions(query);
  }

  @PostMapping("/sync/{providerName}")
  public void syncDestinations(@PathVariable("providerName") BookingProviderName providerName) {
    destinationService.syncDestinationsForProvider(providerName);
  }
}
