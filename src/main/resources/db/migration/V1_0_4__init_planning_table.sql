-- asia_local_guide_gateway.planning

CREATE TABLE `planning` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_planning_user` (`user_id`),
  CONSTRAINT `FK_Planning_User` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
);

-- asia_local_guide_gateway.day_plan

CREATE TABLE `day_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `planning_id` bigint NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_day_plan_planning` (`planning_id`),
  INDEX `idx_day_plan_date` (`date`),
  CONSTRAINT `FK_DayPlan_Planning` FOREIGN KEY (`planning_id`) REFERENCES `planning` (`id`)
);

-- asia_local_guide_gateway.activity

CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_activity_id` varchar(255) NOT NULL,
  `booking_provider_id` bigint NOT NULL,
  `average_rating` double DEFAULT NULL CHECK (`average_rating` >= 0.0 AND `average_rating` <= 5.0),
  `review_count` int DEFAULT NULL CHECK (`review_count` >= 0),
  `duration_minutes` int NOT NULL CHECK (`duration_minutes` >= 1),
  `booking_url` varchar(255) NOT NULL,
  `last_updated` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_activity_business_key` (`provider_activity_id`, `booking_provider_id`),
  INDEX `idx_activity_provider` (`booking_provider_id`),
  CONSTRAINT `FK_Activity_BookingProvider` FOREIGN KEY (`booking_provider_id`) REFERENCES `booking_provider` (`id`)
);

-- asia_local_guide_gateway.activity_translation

CREATE TABLE `activity_translation` (
  `activity_id` bigint NOT NULL,
  `language_code` varchar(10) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  PRIMARY KEY (`activity_id`, `language_code`),
  INDEX `idx_activity_translation_language` (`language_code`),
  CONSTRAINT `FK_ActivityTranslation_Activity` FOREIGN KEY (`activity_id`)
    REFERENCES `activity` (`id`)
);

-- asia_local_guide_gateway.activity_image

CREATE TABLE `activity_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `activity_image_height` int NOT NULL CHECK (`activity_image_height` > 0),
  `activity_image_width` int NOT NULL CHECK (`activity_image_width` > 0),
  `activity_image_url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_activity_image_activity` (`activity_id`),
  CONSTRAINT `FK_ActivityImage_Activity` FOREIGN KEY (`activity_id`)
    REFERENCES `activity` (`id`)
);

-- asia_local_guide_gateway.day_activity

CREATE TABLE `day_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `day_plan_id` bigint NOT NULL,
  `start_time` datetime(6) NOT NULL,
  `end_time` datetime(6) NOT NULL,
  `activity_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_day_activity_day_plan` (`day_plan_id`),
  INDEX `idx_day_activity_activity` (`activity_id`),
  INDEX `idx_day_activity_time_range` (`start_time`, `end_time`),
  CONSTRAINT `FK_DayActivity_DayPlan` FOREIGN KEY (`day_plan_id`) REFERENCES `day_plan` (`id`),
  CONSTRAINT `FK_DayActivity_Activity` FOREIGN KEY (`activity_id`)
    REFERENCES `activity` (`id`),
  CONSTRAINT `chk_time_order` CHECK (`end_time` >= `start_time`)
);