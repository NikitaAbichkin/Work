CREATE SEQUENCE IF NOT EXISTS confirmation_codes_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE confirmation_codes
(
    id         BIGINT       NOT NULL,
    code       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id    BIGINT       NOT NULL,
    CONSTRAINT pk_confirmation_codes PRIMARY KEY (id)
);

ALTER TABLE confirmation_codes
    ADD CONSTRAINT FK_CONFIRMATION_CODES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);