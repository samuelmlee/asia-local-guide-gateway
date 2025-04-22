
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

-- asia_local_guide_gateway.day_activity

CREATE TABLE `day_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `day_plan_id` bigint NOT NULL,
  `provider_activity_id` varchar(255) NOT NULL,
  `provider_name` enum('VIATOR') NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `combined_average_rating` double DEFAULT NULL CHECK (`combined_average_rating` >= 0.0 AND `combined_average_rating` <= 5.0),
  `review_count` int DEFAULT NULL CHECK (`review_count` >= 0),
  `duration_minutes` int DEFAULT NULL CHECK (`duration_minutes` >= 1),
  `from_price` double DEFAULT NULL CHECK (`from_price` >= 0.0),
  `currency` varchar(255) NOT NULL,
  `height` int NOT NULL,
  `width` int NOT NULL,
  `url` varchar(255) NOT NULL,
  `provider_url` varchar(255) NOT NULL,
  `last_updated` datetime NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_DayActivity_DayPlan` FOREIGN KEY (`day_plan_id`) REFERENCES `day_plan` (`id`)
);