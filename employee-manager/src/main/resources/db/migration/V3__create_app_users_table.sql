-- V3: Create the app_users table for JWT authentication
CREATE TABLE app_users (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(150)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    CONSTRAINT uq_app_users_username UNIQUE (username)
);
