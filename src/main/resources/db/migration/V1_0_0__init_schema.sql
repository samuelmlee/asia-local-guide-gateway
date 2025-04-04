-- asia_local_guide_gateway_test.activity_tag definition

CREATE TABLE `activity_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
);


-- asia_local_guide_gateway_test.booking_provider definition

CREATE TABLE `booking_provider` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` enum('VIATOR') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_BookingProvider_Name` (`name`)
);


-- asia_local_guide_gateway_test.country definition

CREATE TABLE `country` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `iso_2_code` varchar(2) NOT NULL,
  PRIMARY KEY (`id`)
);


-- asia_local_guide_gateway_test.activity_tag_provider_mapping definition

CREATE TABLE `activity_tag_provider_mapping` (
  `activity_tag_id` bigint NOT NULL,
  `provider_activity_tag_id` varchar(255) NOT NULL,
  `booking_provider_id` bigint NOT NULL,
  PRIMARY KEY (`activity_tag_id`,`booking_provider_id`),
  KEY `FK_ActivityTagProviderMapping_BookingProvider` (`booking_provider_id`),
  CONSTRAINT `FK_ActivityTagProviderMapping_BookingProvider` FOREIGN KEY (`booking_provider_id`) REFERENCES `booking_provider` (`id`),
  CONSTRAINT `FK_ActivityTagProviderMapping_ActivityTag` FOREIGN KEY (`activity_tag_id`) REFERENCES `activity_tag` (`id`)
);


-- asia_local_guide_gateway_test.activity_tag_translation definition

CREATE TABLE `activity_tag_translation` (
  `language_code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `prompt_text` varchar(255) NOT NULL,
  `activity_tag_id` bigint NOT NULL,
  PRIMARY KEY (`activity_tag_id`,`language_code`),
  CONSTRAINT `FK_ActivityTagTranslation_ActivityTag` FOREIGN KEY (`activity_tag_id`) REFERENCES `activity_tag` (`id`)
);


-- asia_local_guide_gateway_test.country_translation definition

CREATE TABLE `country_translation` (
  `language_code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `country_id` bigint NOT NULL,
  PRIMARY KEY (`country_id`,`language_code`),
  CONSTRAINT `FK_CountryTranslation_Country` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`)
);


-- asia_local_guide_gateway_test.destination definition

CREATE TABLE `destination` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `type` enum('CITY','DISTRICT','OTHER','REGION') NOT NULL,
  `country_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Destination_Country` (`country_id`),
  CONSTRAINT `FK_Destination_Country` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`)
);


-- asia_local_guide_gateway_test.destination_provider_mapping definition

CREATE TABLE `destination_provider_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_destination_id` varchar(255) NOT NULL,
  `destination_id` bigint NOT NULL,
  `provider_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_DestinationProviderMapping_Destination` (`destination_id`),
  KEY `FK_DestinationProviderMapping_BookingProvider` (`provider_id`),
  CONSTRAINT `FK_DestinationProviderMapping_Destination` FOREIGN KEY (`destination_id`) REFERENCES `destination` (`id`),
  CONSTRAINT `FK_DestinationProviderMapping_BookingProvider` FOREIGN KEY (`provider_id`) REFERENCES `booking_provider` (`id`)
);


-- asia_local_guide_gateway_test.destination_translation definition

CREATE TABLE `destination_translation` (
  `language_code` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `destination_id` bigint NOT NULL,
  PRIMARY KEY (`destination_id`,`language_code`),
  CONSTRAINT `FK_DestinationTranslation_Destination` FOREIGN KEY (`destination_id`) REFERENCES `destination` (`id`)
);