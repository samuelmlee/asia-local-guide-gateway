package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.ActivityTagDTO;
import com.asialocalguide.gateway.core.service.activitytag.ActivityTagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/activity-tags")
public class ActivityTagController {

    private final ActivityTagService activityTagService;

    public ActivityTagController(ActivityTagService activityTagService) {
        this.activityTagService = activityTagService;
    }

    @GetMapping
    public List<ActivityTagDTO> getActivityTags() {
        return activityTagService.getActivityTags();
    }
}
