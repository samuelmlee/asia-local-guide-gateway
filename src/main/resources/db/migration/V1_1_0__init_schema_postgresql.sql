-- asia_local_guide_gateway.activity_tag definition

-- Drop table

-- DROP TABLE activity_tag;

CREATE TABLE activity_tag (
	id bigserial NOT NULL,
	CONSTRAINT idx_16435_primary PRIMARY KEY (id)
);


-- asia_local_guide_gateway.booking_provider definition

-- Drop table

-- DROP TABLE booking_provider;

CREATE TABLE booking_provider (
	id bigserial NOT NULL,
	"name" asia_local_guide_gateway.booking_provider_name NOT NULL,
	CONSTRAINT idx_16453_primary PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_16453_uq_bookingprovider_name ON asia_local_guide_gateway.booking_provider USING btree (name);


-- asia_local_guide_gateway.country definition

-- Drop table

-- DROP TABLE country;

CREATE TABLE country (
	id bigserial NOT NULL,
	iso_2_code varchar(2) NOT NULL,
	CONSTRAINT idx_16458_primary PRIMARY KEY (id)
);


-- asia_local_guide_gateway.flyway_schema_history definition

-- Drop table

-- DROP TABLE flyway_schema_history;

CREATE TABLE flyway_schema_history (
	installed_rank int4 NOT NULL,
	"version" varchar(50) NULL,
	description varchar(200) NOT NULL,
	"type" varchar(20) NOT NULL,
	script varchar(1000) NOT NULL,
	checksum int4 NULL,
	installed_by varchar(100) NOT NULL,
	installed_on timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
	execution_time int4 NOT NULL,
	success bool NOT NULL,
	CONSTRAINT idx_16490_primary PRIMARY KEY (installed_rank)
);
CREATE INDEX idx_16490_flyway_schema_history_s_idx ON asia_local_guide_gateway.flyway_schema_history USING btree (success);


-- asia_local_guide_gateway."user" definition

-- Drop table

-- DROP TABLE "user";

CREATE TABLE "user" (
	id bigserial NOT NULL,
	email varchar(255) NOT NULL,
	"name" varchar(255) NULL,
	CONSTRAINT idx_16502_primary PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_16502_uk_user_email ON asia_local_guide_gateway."user" USING btree (email);


-- asia_local_guide_gateway.activity definition

-- Drop table

-- DROP TABLE activity;

CREATE TABLE activity (
	id bytea NOT NULL,
	provider_activity_id varchar(255) NOT NULL,
	booking_provider_id int8 NOT NULL,
	average_rating float8 NULL,
	review_count int4 NULL,
	duration_minutes int4 NOT NULL,
	booking_url varchar(255) NOT NULL,
	last_updated timestamptz NOT NULL,
	CONSTRAINT idx_16423_primary PRIMARY KEY (id),
	CONSTRAINT fk_activity_bookingprovider FOREIGN KEY (booking_provider_id) REFERENCES booking_provider(id)
);
CREATE INDEX idx_16423_idx_activity_provider ON asia_local_guide_gateway.activity USING btree (booking_provider_id);
CREATE UNIQUE INDEX idx_16423_uk_activity_business_key ON asia_local_guide_gateway.activity USING btree (provider_activity_id, booking_provider_id);


-- asia_local_guide_gateway.activity_image definition

-- Drop table

-- DROP TABLE activity_image;

CREATE TABLE activity_image (
	id bytea NOT NULL,
	activity_id bytea NOT NULL,
	height int4 NOT NULL,
	width int4 NOT NULL,
	url varchar(255) NOT NULL,
	"type" asia_local_guide_gateway.activity_image_type DEFAULT 'MOBILE'::activity_image_type NOT NULL,
	CONSTRAINT idx_16428_primary PRIMARY KEY (id),
	CONSTRAINT fk_activityimage_activity FOREIGN KEY (activity_id) REFERENCES activity(id)
);
CREATE INDEX idx_16428_idx_activity_image_activity ON asia_local_guide_gateway.activity_image USING btree (activity_id);


-- asia_local_guide_gateway.activity_tag_provider_mapping definition

-- Drop table

-- DROP TABLE activity_tag_provider_mapping;

CREATE TABLE activity_tag_provider_mapping (
	activity_tag_id int8 NOT NULL,
	provider_activity_tag_id varchar(255) NOT NULL,
	booking_provider_id int8 NOT NULL,
	CONSTRAINT idx_16439_primary PRIMARY KEY (activity_tag_id, booking_provider_id),
	CONSTRAINT fk_activitytagprovidermapping_activitytag FOREIGN KEY (activity_tag_id) REFERENCES activity_tag(id),
	CONSTRAINT fk_activitytagprovidermapping_bookingprovider FOREIGN KEY (booking_provider_id) REFERENCES booking_provider(id)
);
CREATE INDEX idx_16439_fk_activitytagprovidermapping_bookingprovider ON asia_local_guide_gateway.activity_tag_provider_mapping USING btree (booking_provider_id);


-- asia_local_guide_gateway.activity_tag_translation definition

-- Drop table

-- DROP TABLE activity_tag_translation;

CREATE TABLE activity_tag_translation (
	language_code varchar(255) NOT NULL,
	"name" varchar(255) NOT NULL,
	prompt_text varchar(255) NOT NULL,
	activity_tag_id int8 NOT NULL,
	CONSTRAINT idx_16442_primary PRIMARY KEY (activity_tag_id, language_code),
	CONSTRAINT fk_activitytagtranslation_activitytag FOREIGN KEY (activity_tag_id) REFERENCES activity_tag(id)
);


-- asia_local_guide_gateway.activity_translation definition

-- Drop table

-- DROP TABLE activity_translation;

CREATE TABLE activity_translation (
	activity_id bytea NOT NULL,
	language_code varchar(10) NOT NULL,
	title varchar(255) NOT NULL,
	description text NULL,
	CONSTRAINT idx_16447_primary PRIMARY KEY (activity_id, language_code),
	CONSTRAINT fk_activitytranslation_activity FOREIGN KEY (activity_id) REFERENCES activity(id)
);
CREATE INDEX idx_16447_idx_activity_translation_language ON asia_local_guide_gateway.activity_translation USING btree (language_code);


-- asia_local_guide_gateway.country_translation definition

-- Drop table

-- DROP TABLE country_translation;

CREATE TABLE country_translation (
	language_code varchar(255) NOT NULL,
	"name" varchar(255) NOT NULL,
	country_id int8 NOT NULL,
	CONSTRAINT idx_16462_primary PRIMARY KEY (country_id, language_code),
	CONSTRAINT fk_countrytranslation_country FOREIGN KEY (country_id) REFERENCES country(id)
);
CREATE INDEX idx_16462_idx_country_translation_lang_name ON asia_local_guide_gateway.country_translation USING btree (language_code, name);


-- asia_local_guide_gateway.destination definition

-- Drop table

-- DROP TABLE destination;

CREATE TABLE destination (
	id bigserial NOT NULL,
	latitude float8 NULL,
	longitude float8 NULL,
	"type" asia_local_guide_gateway.destination_type NOT NULL,
	country_id int8 NOT NULL,
	CONSTRAINT idx_16478_primary PRIMARY KEY (id),
	CONSTRAINT fk_destination_country FOREIGN KEY (country_id) REFERENCES country(id)
);
CREATE INDEX idx_16478_fk_destination_country ON asia_local_guide_gateway.destination USING btree (country_id);
CREATE INDEX idx_16478_idx_destination_type ON asia_local_guide_gateway.destination USING btree (type);


-- asia_local_guide_gateway.destination_provider_mapping definition

-- Drop table

-- DROP TABLE destination_provider_mapping;

CREATE TABLE destination_provider_mapping (
	destination_id int8 NOT NULL,
	booking_provider_id int8 NOT NULL,
	provider_destination_id varchar(255) NOT NULL,
	CONSTRAINT idx_16482_primary PRIMARY KEY (destination_id, booking_provider_id),
	CONSTRAINT fk_destinationprovidermapping_bookingprovider FOREIGN KEY (booking_provider_id) REFERENCES booking_provider(id),
	CONSTRAINT fk_destinationprovidermapping_destination FOREIGN KEY (destination_id) REFERENCES destination(id)
);
CREATE INDEX idx_16482_fk_destinationprovidermapping_bookingprovider ON asia_local_guide_gateway.destination_provider_mapping USING btree (booking_provider_id);


-- asia_local_guide_gateway.destination_translation definition

-- Drop table

-- DROP TABLE destination_translation;

CREATE TABLE destination_translation (
	language_code varchar(255) NOT NULL,
	"name" varchar(255) NULL,
	destination_id int8 NOT NULL,
	CONSTRAINT idx_16485_primary PRIMARY KEY (destination_id, language_code),
	CONSTRAINT fk_destinationtranslation_destination FOREIGN KEY (destination_id) REFERENCES destination(id)
);
CREATE INDEX idx_16485_idx_dest_translation_lang_name ON asia_local_guide_gateway.destination_translation USING btree (language_code, name);
CREATE INDEX idx_16485_idx_dest_translation_name_fulltext ON asia_local_guide_gateway.destination_translation USING gin (to_tsvector('simple'::regconfig, (name)::text));


-- asia_local_guide_gateway.planning definition

-- Drop table

-- DROP TABLE planning;

CREATE TABLE planning (
	id bytea NOT NULL,
	"name" varchar(255) NOT NULL,
	user_id int8 NOT NULL,
	created_date timestamptz NOT NULL,
	CONSTRAINT idx_16496_primary PRIMARY KEY (id),
	CONSTRAINT fk_planning_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);
CREATE INDEX idx_16496_idx_planning_user ON asia_local_guide_gateway.planning USING btree (user_id);


-- asia_local_guide_gateway.user_auth definition

-- Drop table

-- DROP TABLE user_auth;

CREATE TABLE user_auth (
	user_id int8 NOT NULL,
	auth_provider_name asia_local_guide_gateway.user_auth_auth_provider_name NOT NULL,
	provider_user_id varchar(255) NOT NULL,
	CONSTRAINT idx_16508_primary PRIMARY KEY (user_id, auth_provider_name),
	CONSTRAINT fk_userauth_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_16508_uk_userauth_provider_providerid ON asia_local_guide_gateway.user_auth USING btree (auth_provider_name, provider_user_id);


-- asia_local_guide_gateway.day_plan definition

-- Drop table

-- DROP TABLE day_plan;

CREATE TABLE day_plan (
	id bytea NOT NULL,
	planning_id bytea NOT NULL,
	"date" date NOT NULL,
	CONSTRAINT idx_16472_primary PRIMARY KEY (id),
	CONSTRAINT fk_dayplan_planning FOREIGN KEY (planning_id) REFERENCES planning(id)
);
CREATE INDEX idx_16472_idx_day_plan_date ON asia_local_guide_gateway.day_plan USING btree (date);
CREATE INDEX idx_16472_idx_day_plan_planning ON asia_local_guide_gateway.day_plan USING btree (planning_id);


-- asia_local_guide_gateway.day_activity definition

-- Drop table

-- DROP TABLE day_activity;

CREATE TABLE day_activity (
	id bytea NOT NULL,
	day_plan_id bytea NOT NULL,
	start_time timestamptz NOT NULL,
	end_time timestamptz NOT NULL,
	activity_id bytea NOT NULL,
	CONSTRAINT idx_16467_primary PRIMARY KEY (id),
	CONSTRAINT fk_dayactivity_activity FOREIGN KEY (activity_id) REFERENCES activity(id),
	CONSTRAINT fk_dayactivity_dayplan FOREIGN KEY (day_plan_id) REFERENCES day_plan(id)
);
CREATE INDEX idx_16467_idx_day_activity_activity ON asia_local_guide_gateway.day_activity USING btree (activity_id);
CREATE INDEX idx_16467_idx_day_activity_day_plan ON asia_local_guide_gateway.day_activity USING btree (day_plan_id);
CREATE INDEX idx_16467_idx_day_activity_time_range ON asia_local_guide_gateway.day_activity USING btree (start_time, end_time);