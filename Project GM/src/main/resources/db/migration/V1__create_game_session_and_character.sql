CREATE TABLE player_character (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level INTEGER NOT NULL,
    experience INTEGER NOT NULL,
    strength INTEGER NOT NULL,
    agility INTEGER NOT NULL,
    intellect INTEGER NOT NULL,
    willpower INTEGER NOT NULL,
    perception INTEGER NOT NULL,
    presence INTEGER NOT NULL,
    health_maximum INTEGER NOT NULL,
    health_current INTEGER NOT NULL,
    health_wounds INTEGER NOT NULL
);

CREATE TABLE game_session (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    world_id UUID NOT NULL,
    current_location_id UUID,
    status VARCHAR(30) NOT NULL,
    world_time TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    character_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_game_session_character
        FOREIGN KEY (character_id)
        REFERENCES player_character (id),
    CONSTRAINT ck_game_session_status
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'PLAYER_DEAD'))
);

CREATE INDEX idx_game_session_player_id
    ON game_session (player_id);

CREATE INDEX idx_game_session_world_id
    ON game_session (world_id);
