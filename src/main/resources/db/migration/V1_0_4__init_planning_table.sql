-- asia_local_guide_gateway.planning

CREATE TABLE `planning` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_Planning_User` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
);

-- asia_local_guide_gateway.day_plan

CREATE TABLE `day_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `planning_id` bigint NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_DayPlan_Planning` FOREIGN KEY (`planning_id`) REFERENCES `planning` (`id`)
);

-- asia_local_guide_gateway.activity

CREATE TABLE `activity` (
  `provider_activity_id` varchar(255) NOT NULL,
  `booking_provider_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `combined_average_rating` double NOT NULL CHECK (`combined_average_rating` >= 0.0 AND `combined_average_rating` <= 5.0),
  `review_count` int NOT NULL CHECK (`review_count` >= 0),
  `duration_minutes` int NOT NULL CHECK (`duration_minutes` >= 1),
  `from_price` double NOT NULL CHECK (`from_price` >= 0.0),
  `currency` varchar(255) NOT NULL,
  `activity_image_height` int NOT NULL,
  `activity_image_width` int NOT NULL,
  `activity_image_url` varchar(255) NOT NULL,
  `provider_url` varchar(255) NOT NULL,
  `last_updated` datetime NOT NULL,
  PRIMARY KEY (`provider_activity_id`, `booking_provider_id`)
);

-- asia_local_guide_gateway.day_activity

CREATE TABLE `day_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `day_plan_id` bigint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `provider_activity_id` varchar(255) NOT NULL,
  `booking_provider_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_DayActivity_DayPlan` FOREIGN KEY (`day_plan_id`) REFERENCES `day_plan` (`id`),
  CONSTRAINT `FK_DayActivity_Activity` FOREIGN KEY (`provider_activity_id`, `booking_provider_id`)
    REFERENCES `activity` (`provider_activity_id`, `booking_provider_id`),
  CONSTRAINT `chk_time_order` CHECK (end_time >= start_time)
);