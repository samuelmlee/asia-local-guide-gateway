package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.ActivityTag;
import com.asialocalguide.gateway.core.repository.ActivityTagRepository;
import com.asialocalguide.gateway.viator.service.ViatorActivityTagService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ActivityTagService {

  private final ViatorActivityTagService viatorActivityTagService;

  private final ActivityTagRepository activityTagRepository;

  public ActivityTagService(
      ViatorActivityTagService viatorActivityTagService,
      ActivityTagRepository activityTagRepository) {
    this.viatorActivityTagService = viatorActivityTagService;
    this.activityTagRepository = activityTagRepository;
  }

  public void syncViatorActivityTags() {

    List<ActivityTag> activityTags = viatorActivityTagService.getAllDestinations();

    activityTagRepository.saveAll(activityTags);
  }
}
