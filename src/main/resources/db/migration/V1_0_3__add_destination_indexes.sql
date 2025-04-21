CREATE FULLTEXT INDEX idx_dest_translation_name_fulltext ON destination_translation(name);

CREATE INDEX idx_dest_translation_lang_name ON destination_translation(language_code, name);

CREATE INDEX idx_destination_type ON destination(type);

CREATE INDEX idx_country_translation_lang_name ON country_translation(language_code, name);