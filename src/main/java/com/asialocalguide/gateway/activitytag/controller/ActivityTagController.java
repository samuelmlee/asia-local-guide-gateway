package com.asialocalguide.gateway.activitytag.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asialocalguide.gateway.activitytag.dto.ActivityTagDTO;
import com.asialocalguide.gateway.activitytag.service.ActivityTagService;

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
