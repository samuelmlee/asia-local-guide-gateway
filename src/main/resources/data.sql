INSERT INTO `booking_provider` (`id`, `name`)
SELECT 1, 'VIATOR' FROM DUAL
WHERE NOT EXISTS (SELECT * FROM `booking_provider`
      WHERE `id`=1 AND `name`='VIATOR' LIMIT 1);