CREATE TABLE results (
    id          BIGSERIAL    PRIMARY KEY,
    goal_id     BIGINT       NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_results_goal_id ON results(goal_id);

CREATE TABLE result_images (
    result_id   BIGINT       NOT NULL REFERENCES results(id) ON DELETE CASCADE,
    image_url   TEXT         NOT NULL
);

CREATE INDEX idx_result_images_result_id ON result_images(result_id);