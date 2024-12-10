package com.asialocalguide.gateway.controller;

import com.asialocalguide.gateway.dto.DestinationDTO;
import com.asialocalguide.gateway.service.AuxiliaryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auxiliary")
public class AuxiliaryController {

  @Autowired private AuxiliaryService auxiliaryService;

  @GetMapping
  public List<DestinationDTO> getAllDestinations() {
    return auxiliaryService.getAllDestinations();
  }
}
