package com.asialocalguide.gateway.activitytag.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asialocalguide.gateway.activitytag.dto.ActivityTagDTO;
import com.asialocalguide.gateway.activitytag.service.ActivityTagService;

/**
 * REST controller for managing activity tags.
 * Provides endpoints to retrieve application activity tag information.
 */
@RestController
@RequestMapping("/v1/activity-tags")
public class ActivityTagController {

	private final ActivityTagService activityTagService;

	public ActivityTagController(ActivityTagService activityTagService) {
		this.activityTagService = activityTagService;
	}

	/**
	 * Retrieves all application activity tags that have mappings to provider's activity tags. 
	 * The response is localized based on the Accept-Language header in the request.
	 *
	 * @return a list of all available activity tags
	 */
	@GetMapping
	public List<ActivityTagDTO> getActivityTags() {
		return activityTagService.getActivityTags();
	}
}