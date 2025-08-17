-- localhost
CREATE TABLE `category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `UK_46ccwnsi9409t36lurvtyljak` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `place` (
  `place_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `name` varchar(255) DEFAULT NULL,
  `seat_quantity` int DEFAULT NULL,
  `seat_type` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `tel` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`place_id`),
  CONSTRAINT `chk_seat_quantity` CHECK ((`seat_quantity` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `product` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `description` varchar(255) DEFAULT NULL,
  `release_date` date DEFAULT NULL,
  `running_time` int DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FK1mtsbur82frn64de7balymq9s` (`category_id`),
  CONSTRAINT `FK1mtsbur82frn64de7balymq9s` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`),
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_running_time` CHECK ((`running_time` >= 60))
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `product_price` (
  `product_seat_price_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `price` int DEFAULT NULL,
  `seat_type` varchar(255) NOT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`product_seat_price_id`),
  KEY `FKeupemu63ifqfc4txkskyy1hyi` (`product_id`),
  CONSTRAINT `FKeupemu63ifqfc4txkskyy1hyi` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- reservationDB.product_revenue definition

CREATE TABLE `product_revenue` (
  `product_id` bigint NOT NULL,
  `total_revenue` bigint NOT NULL,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- reservationDB.product_seat_schedule definition

CREATE TABLE `product_seat_schedule` (
  `product_seat_schedule_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `event_date_time` timestamp NULL DEFAULT NULL,
  `reserved_quantity` int DEFAULT NULL,
  `seat_type` varchar(255) DEFAULT NULL,
  `place_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`product_seat_schedule_id`),
  KEY `product_id` (`product_id`),
  KEY `FK855xh2iynmujofy1aukiyueyy` (`place_id`),
  CONSTRAINT `FK855xh2iynmujofy1aukiyueyy` FOREIGN KEY (`place_id`) REFERENCES `place` (`place_id`),
  CONSTRAINT `product_seat_schedule_ibfk_1` FOREIGN KEY (`place_id`) REFERENCES `place` (`place_id`) ON DELETE CASCADE,
  CONSTRAINT `product_seat_schedule_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- reservationDB.reservation definition

CREATE TABLE `reservation` (
  `reservation_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reservation_status` varchar(255) DEFAULT NULL,
  `reserved_date` date DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`reservation_id`),
  KEY `FK68999qe28ym9eqqlowybh9nvn` (`member_id`),
  KEY `FKgoouhtuwwm277879njd9atahw` (`product_id`),
  CONSTRAINT `FK68999qe28ym9eqqlowybh9nvn` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKgoouhtuwwm277879njd9atahw` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`),
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- reservationDB.reservation_price definition

CREATE TABLE `reservation_price` (
  `reservation_price_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `reserved_price` int DEFAULT NULL,
  `reserved_quantity` int DEFAULT NULL,
  `reserved_seat_type` varchar(255) DEFAULT NULL,
  `product_seat_schedule_id` bigint DEFAULT NULL,
  `reservation_id` bigint DEFAULT NULL,
  PRIMARY KEY (`reservation_price_id`),
  KEY `FKt3b4x9w61xyjck3cvcexwoois` (`product_seat_schedule_id`),
  KEY `FKmk1bubfxiiwyfdvl8muuyhl3q` (`reservation_id`),
  CONSTRAINT `FKmk1bubfxiiwyfdvl8muuyhl3q` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`reservation_id`),
  CONSTRAINT `FKt3b4x9w61xyjck3cvcexwoois` FOREIGN KEY (`product_seat_schedule_id`) REFERENCES `product_seat_schedule` (`product_seat_schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;