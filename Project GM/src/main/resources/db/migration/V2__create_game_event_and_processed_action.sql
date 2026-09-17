CREATE TABLE game_event (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    sequence BIGINT NOT NULL,
    type VARCHAR(60) NOT NULL,
    actor_id UUID,
    payload JSONB NOT NULL,
    world_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_game_event_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id),
    CONSTRAINT uq_game_event_session_sequence
        UNIQUE (session_id, sequence)
);

CREATE INDEX idx_game_event_session_id
    ON game_event (session_id);

CREATE TABLE processed_action (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    idempotency_key UUID NOT NULL,
    action_id UUID NOT NULL,
    response_payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_processed_action_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id),
    CONSTRAINT uq_processed_action_session_key
        UNIQUE (session_id, idempotency_key)
);

CREATE INDEX idx_processed_action_session_id
    ON processed_action (session_id);
