package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.ActivityTag;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityTagDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ViatorActivityTagService {

  private final ViatorClient viatorClient;

  public ViatorActivityTagService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<ActivityTag> getAllDestinations() {

    List<ViatorActivityTagDTO> activityTagDTOs = viatorClient.getAllActivityTags();

    return activityTagDTOs.stream()
        .map(
            dto ->
                ActivityTag.builder()
                    .id(dto.tagId())
                    .name(dto.allNamesByLocale().en())
                    .parentTagId(dto.parentTagIds().getFirst())
                    .build())
        .toList();
  }
}
