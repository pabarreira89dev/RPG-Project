CREATE TABLE game_event (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    sequence BIGINT NOT NULL,
    type VARCHAR(60) NOT NULL,
    actor_id CHAR(36),
    payload JSON NOT NULL,
    world_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_game_event_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id),
    CONSTRAINT uq_game_event_session_sequence
        UNIQUE (session_id, sequence)
);

CREATE INDEX idx_game_event_session_id
    ON game_event (session_id);

CREATE TABLE processed_action (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    idempotency_key CHAR(36) NOT NULL,
    action_id CHAR(36) NOT NULL,
    response_payload JSON NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_processed_action_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id),
    CONSTRAINT uq_processed_action_session_key
        UNIQUE (session_id, idempotency_key)
);

CREATE INDEX idx_processed_action_session_id
    ON processed_action (session_id);
