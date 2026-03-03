CREATE SEQUENCE IF NOT EXISTS refresh_tokens_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE refresh_tokens
(
    id         BIGINT                      NOT NULL,
    token      VARCHAR(255)                NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id    BIGINT                      NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);