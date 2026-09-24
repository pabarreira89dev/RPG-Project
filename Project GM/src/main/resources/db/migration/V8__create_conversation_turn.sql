CREATE TABLE conversation_turn (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    sequence BIGINT NOT NULL,
    player_text VARCHAR(2000) NOT NULL,
    narration VARCHAR(2000) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT uq_conversation_turn_session_sequence UNIQUE (session_id, sequence),
    CONSTRAINT fk_conversation_turn_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id)
);

CREATE INDEX ix_conversation_turn_session_id ON conversation_turn (session_id);
