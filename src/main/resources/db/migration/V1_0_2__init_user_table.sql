-- User table definition
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `UK_User_Email` UNIQUE (`email`)
);

-- UserAuth table definition
CREATE TABLE `user_auth` (
  `user_id` bigint NOT NULL,
  `auth_provider_name` enum('FIREBASE') NOT NULL,
  `provider_user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`, `auth_provider_name`),
  CONSTRAINT `FK_UserAuth_User` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `UK_UserAuth_Provider_ProviderId` UNIQUE (`auth_provider_name`, `provider_user_id`)
);