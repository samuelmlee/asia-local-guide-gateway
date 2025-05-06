
ALTER TABLE `activity_image` ADD `type` enum('DESKTOP', 'MOBILE') NOT NULL DEFAULT 'MOBILE';

ALTER TABLE `activity_image` DROP CHECK `activity_image_chk_1`;
ALTER TABLE `activity_image` DROP CHECK `activity_image_chk_2`;

ALTER TABLE `activity_image` RENAME COLUMN `activity_image_height` TO `height`;
ALTER TABLE `activity_image` RENAME COLUMN `activity_image_width` TO `width`;
ALTER TABLE `activity_image` RENAME COLUMN `activity_image_url` TO `url`;

ALTER TABLE `activity_image` ADD CONSTRAINT `activity_image_chk_image_size` CHECK (`height` > 0 AND `width` > 0);
