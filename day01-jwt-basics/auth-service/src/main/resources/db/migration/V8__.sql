CREATE TABLE stage_result_images
(
    stage_id BIGINT NOT NULL,
    images   VARCHAR(255)
);

ALTER TABLE goals
    ADD daily_time_minutes INTEGER;

ALTER TABLE goals
    ADD deadline date;

ALTER TABLE goals
    ADD priority VARCHAR(255);

ALTER TABLE goals
    ADD progress INTEGER;

ALTER TABLE goals
    ADD start_date date;

ALTER TABLE stages
    ADD deadline time WITHOUT TIME ZONE;

ALTER TABLE stages
    ADD description TEXT;

ALTER TABLE stages
    ADD estimated_time VARCHAR(255);

ALTER TABLE stages
    ADD priority VARCHAR(255);

ALTER TABLE stages
    ADD progress INTEGER;

ALTER TABLE stages
    ADD result_text VARCHAR(255);

ALTER TABLE stages
    ADD starts_at time WITHOUT TIME ZONE;

ALTER TABLE stages
    ADD status VARCHAR(20);

ALTER TABLE goals
    ALTER COLUMN priority SET NOT NULL;

ALTER TABLE stages
    ALTER COLUMN progress SET NOT NULL;

ALTER TABLE goals
    ALTER COLUMN start_date SET NOT NULL;

ALTER TABLE stages
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE stage_result_images
    ADD CONSTRAINT fk_stage_result_images_on_stage FOREIGN KEY (stage_id) REFERENCES stages (id);

ALTER TABLE confirmation_codes
    ALTER COLUMN code TYPE VARCHAR(4) USING (code::VARCHAR(4));

ALTER TABLE goals
    ALTER COLUMN status TYPE VARCHAR(255) USING (status::VARCHAR(255));