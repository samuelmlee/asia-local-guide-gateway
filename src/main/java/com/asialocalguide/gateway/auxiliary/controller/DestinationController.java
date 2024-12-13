package com.asialocalguide.gateway.auxiliary.controller;

import com.asialocalguide.gateway.auxiliary.dto.DestinationDTO;
import com.asialocalguide.gateway.auxiliary.service.DestinationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auxiliary")
public class DestinationController {

  @Autowired private DestinationService destinationService;

  @GetMapping("/destinations")
  public List<DestinationDTO> getAllDestinations() {
    return destinationService.getAllDestinations();
  }
}
