-- User table definition
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- UserAuth table definition
CREATE TABLE `user_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auth_provider_name` enum('FIREBASE') NOT NULL,
  `user_id` bigint NOT NULL,
  `provider_user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_UserAuth_User` (`user_id`),
  CONSTRAINT `FK_UserAuth_User` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
);

-- UserAuth unique constraint
ALTER TABLE `user_auth`
ADD CONSTRAINT `UK_UserAuth_User_Provider` UNIQUE (`user_id`, `auth_provider_name`);