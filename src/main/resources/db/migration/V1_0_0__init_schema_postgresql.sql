-- activity_tag definition
CREATE TABLE activity_tag (
    id bigint NOT NULL,
    CONSTRAINT pk_activity_tag PRIMARY KEY (id)
);

-- language definition
CREATE TABLE language (
    id int NOT NULL,
    code varchar(2) NOT NULL,
    CONSTRAINT pk_language PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_language_code ON language USING btree (code);


-- booking_provider definition
CREATE TYPE booking_provider_name AS ENUM (
    'VIATOR',
    'GET_YOUR_GUIDE'
);


CREATE TABLE booking_provider (
    id bigint NOT NULL,
    "name" booking_provider_name NOT NULL,
    CONSTRAINT pk_booking_provider PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_booking_provider_name ON booking_provider USING btree (name);

-- country definition
CREATE TABLE country (
    id bigint NOT NULL,
    iso_2_code varchar(2) NOT NULL,
    CONSTRAINT pk_country PRIMARY KEY (id)
);

-- "user" definition
CREATE TABLE "user" (
    id bigint NOT NULL,
    email varchar(255) NOT NULL,
    "name" varchar(255) NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_user_email ON "user" USING btree (email);

-- activity definition
CREATE TABLE activity (
    id uuid NOT NULL,
    provider_activity_id varchar(255) NOT NULL,
    booking_provider_id bigint NOT NULL,
    average_rating real NULL,
    review_count int NULL,
    duration_minutes int NOT NULL,
    booking_url varchar(255) NOT NULL,
    last_updated timestamptz NOT NULL,
    CONSTRAINT pk_activity PRIMARY KEY (id),
    CONSTRAINT fk_activity_to_booking_provider FOREIGN KEY (booking_provider_id) REFERENCES booking_provider(id)
);
CREATE INDEX idx_activity_provider_id ON activity USING btree (booking_provider_id);
CREATE UNIQUE INDEX uq_activity_provider_id_booking_provider ON activity USING btree (provider_activity_id, booking_provider_id);

-- activity_image definition
CREATE TYPE image_type AS ENUM (
    'MOBILE',
    'DESKTOP',
    'THUMBNAIL'
);

CREATE TABLE activity_image (
    id uuid NOT NULL,
    activity_id uuid NOT NULL,
    height int NOT NULL,
    width int NOT NULL,
    url varchar(255) NOT NULL,
    "type" image_type DEFAULT 'MOBILE'::image_type NOT NULL,
    CONSTRAINT pk_activity_image PRIMARY KEY (id),
    CONSTRAINT fk_activity_image_to_activity FOREIGN KEY (activity_id) REFERENCES activity(id)
);
CREATE INDEX idx_activity_image_activity_id ON activity_image USING btree (activity_id);

-- activity_tag_provider_mapping definition
CREATE TABLE activity_tag_provider_mapping (
    activity_tag_id bigint NOT NULL,
    provider_activity_tag_id varchar(255) NOT NULL,
    booking_provider_id bigint NOT NULL,
    CONSTRAINT pk_activity_tag_provider_mapping PRIMARY KEY (activity_tag_id, booking_provider_id),
    CONSTRAINT fk_activity_tag_provider_mapping_to_activity_tag FOREIGN KEY (activity_tag_id) REFERENCES activity_tag(id),
    CONSTRAINT fk_activity_tag_provider_mapping_to_booking_provider FOREIGN KEY (booking_provider_id) REFERENCES booking_provider(id)
);
CREATE INDEX idx_activity_tag_provider_mapping_booking_provider_id ON activity_tag_provider_mapping USING btree (booking_provider_id);

-- activity_tag_translation definition
CREATE TABLE activity_tag_translation (
    language_id int NOT NULL,
    "name" varchar(255) NOT NULL,
    prompt_text varchar(255) NOT NULL,
    activity_tag_id bigint NOT NULL,
    CONSTRAINT pk_activity_tag_translation PRIMARY KEY (activity_tag_id, language_id),
    CONSTRAINT fk_activity_tag_translation_to_activity_tag FOREIGN KEY (activity_tag_id) REFERENCES activity_tag(id),
    CONSTRAINT fk_activity_tag_translation_to_language FOREIGN KEY (language_id) REFERENCES language(id)
);

-- activity_translation definition
CREATE TABLE activity_translation (
    activity_id uuid NOT NULL,
    language_id int NOT NULL,
    title varchar(255) NOT NULL,
    description text NULL,
    CONSTRAINT pk_activity_translation PRIMARY KEY (activity_id, language_id),
    CONSTRAINT fk_activity_translation_to_activity FOREIGN KEY (activity_id) REFERENCES activity(id),
    CONSTRAINT fk_activity_translation_to_language FOREIGN KEY (language_id) REFERENCES language(id)
);
CREATE INDEX idx_activity_translation_language_id ON activity_translation USING btree (language_id);

-- country_translation definition
CREATE TABLE country_translation (
    language_id int NOT NULL,
    "name" varchar(255) NOT NULL,
    country_id bigint NOT NULL,
    CONSTRAINT pk_country_translation PRIMARY KEY (country_id, language_id),
    CONSTRAINT fk_country_translation_to_country FOREIGN KEY (country_id) REFERENCES country(id),
    CONSTRAINT fk_country_translation_to_language FOREIGN KEY (language_id) REFERENCES language(id)
);
CREATE INDEX idx_country_translation_language_id_name ON country_translation USING btree (language_id, name);

-- destination definition
CREATE TYPE destination_type AS ENUM (
  'REGION',
  'CITY',
  'DISTRICT',
  'OTHER'
);

CREATE TABLE destination (
    id bigint NOT NULL,
    latitude double precision NULL,
    longitude double precision NULL,
    "type" destination_type NOT NULL,
    country_id bigint NOT NULL,
    CONSTRAINT pk_destination PRIMARY KEY (id),
    CONSTRAINT fk_destination_to_country FOREIGN KEY (country_id) REFERENCES country(id)
);
CREATE INDEX idx_destination_country_id ON destination USING btree (country_id);
CREATE INDEX idx_destination_type ON destination USING btree (type);

-- destination_provider_mapping definition
CREATE TABLE destination_provider_mapping (
    destination_id bigint NOT NULL,
    booking_provider_id bigint NOT NULL,
    provider_destination_id varchar(255) NOT NULL,
    CONSTRAINT pk_destination_provider_mapping PRIMARY KEY (destination_id, booking_provider_id),
    CONSTRAINT fk_destination_provider_mapping_to_booking_provider FOREIGN KEY (booking_provider_id) REFERENCES booking_provider(id),
    CONSTRAINT fk_destination_provider_mapping_to_destination FOREIGN KEY (destination_id) REFERENCES destination(id)
);
CREATE INDEX idx_destination_provider_mapping_booking_provider_id ON destination_provider_mapping USING btree (booking_provider_id);

-- destination_translation definition
CREATE TABLE destination_translation (
    language_id int NOT NULL,
    destination_id bigint NOT NULL,
    "name" varchar(255) NULL,
    CONSTRAINT pk_destination_translation PRIMARY KEY (destination_id, language_id),
    CONSTRAINT fk_destination_translation_to_destination FOREIGN KEY (destination_id) REFERENCES destination(id),
    CONSTRAINT fk_destination_translation_to_language FOREIGN KEY (language_id) REFERENCES language(id)
);
CREATE INDEX idx_destination_translation_language_id_name ON destination_translation USING btree (language_id, name);
CREATE INDEX idx_destination_translation_name_fulltext ON destination_translation USING gin (to_tsvector('simple'::regconfig, (name)::text));


-- user_auth definition
CREATE TYPE auth_provider_name AS ENUM (
    'FIREBASE',
    'GOOGLE',
    'FACEBOOK',
    'APPLE'
);

CREATE TABLE user_auth (
    user_id bigint NOT NULL,
    auth_provider_name auth_provider_name NOT NULL,
    provider_user_id varchar(255) NOT NULL,
    CONSTRAINT pk_user_auth PRIMARY KEY (user_id, auth_provider_name),
    CONSTRAINT fk_user_auth_to_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_user_auth_provider_user_id ON user_auth USING btree (auth_provider_name, provider_user_id);


-- planning definition
CREATE TABLE planning (
    id uuid NOT NULL,
    "name" varchar(255) NOT NULL,
    user_id bigint NOT NULL,
    created_date timestamptz NOT NULL,
    CONSTRAINT pk_planning PRIMARY KEY (id),
    CONSTRAINT fk_planning_to_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);
CREATE INDEX idx_planning_user_id ON planning USING btree (user_id);

-- day_plan definition
CREATE TABLE day_plan (
    id uuid NOT NULL,
    planning_id uuid NOT NULL,
    "date" date NOT NULL,
    CONSTRAINT pk_day_plan PRIMARY KEY (id),
    CONSTRAINT fk_day_plan_to_planning FOREIGN KEY (planning_id) REFERENCES planning(id)
);
CREATE INDEX idx_day_plan_date ON day_plan USING btree (date);
CREATE INDEX idx_day_plan_planning_id ON day_plan USING btree (planning_id);

-- day_activity definition
CREATE TABLE day_activity (
    id uuid NOT NULL,
    day_plan_id uuid NOT NULL,
    start_time timestamptz NOT NULL,
    end_time timestamptz NOT NULL,
    activity_id uuid NOT NULL,
    CONSTRAINT pk_day_activity PRIMARY KEY (id),
    CONSTRAINT fk_day_activity_to_activity FOREIGN KEY (activity_id) REFERENCES activity(id),
    CONSTRAINT fk_day_activity_to_day_plan FOREIGN KEY (day_plan_id) REFERENCES day_plan(id)
);
CREATE INDEX idx_day_activity_activity_id ON day_activity USING btree (activity_id);
CREATE INDEX idx_day_activity_day_plan_id ON day_activity USING btree (day_plan_id);
CREATE INDEX idx_day_activity_time_range ON day_activity USING btree (start_time, end_time);