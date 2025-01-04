package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.service.ActivityTagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/activity-tags")
public class ActivityTagController {

  private final ActivityTagService activityTagService;

  public ActivityTagController(ActivityTagService activityTagService) {
    this.activityTagService = activityTagService;
  }

  @PostMapping("/sync/viator")
  public void syncViatorActivityTags() {
    activityTagService.syncViatorActivityTags();
  }
}
