

CREATE UNIQUE INDEX uq_app_user_id_name ON planning USING btree (app_user_id, name);
