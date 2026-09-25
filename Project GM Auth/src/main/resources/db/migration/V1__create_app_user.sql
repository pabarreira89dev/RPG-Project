CREATE TABLE app_user (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    created_at DATETIME NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX idx_app_user_username ON app_user (username);
