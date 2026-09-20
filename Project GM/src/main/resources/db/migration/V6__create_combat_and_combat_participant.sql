ALTER TABLE npc
    ADD COLUMN strength INT NOT NULL DEFAULT 10,
    ADD COLUMN agility INT NOT NULL DEFAULT 10,
    ADD COLUMN intellect INT NOT NULL DEFAULT 10,
    ADD COLUMN willpower INT NOT NULL DEFAULT 10,
    ADD COLUMN perception INT NOT NULL DEFAULT 10,
    ADD COLUMN presence INT NOT NULL DEFAULT 10,
    ADD COLUMN health_maximum INT NOT NULL DEFAULT 20;

UPDATE npc SET strength = 11, agility = 12, intellect = 10, willpower = 10, perception = 11, presence = 13, health_maximum = 18
    WHERE code = 'aron_tavernkeeper';
UPDATE npc SET strength = 14, agility = 12, intellect = 9, willpower = 12, perception = 11, presence = 10, health_maximum = 30
    WHERE code = 'village_guard';
UPDATE npc SET strength = 8, agility = 9, intellect = 14, willpower = 15, perception = 13, presence = 12, health_maximum = 14
    WHERE code = 'village_elder';
UPDATE npc SET strength = 12, agility = 15, intellect = 10, willpower = 11, perception = 14, presence = 9, health_maximum = 24
    WHERE code = 'forest_hunter';
UPDATE npc SET strength = 9, agility = 11, intellect = 12, willpower = 10, perception = 10, presence = 13, health_maximum = 16
    WHERE code = 'traveling_merchant';

CREATE TABLE combat (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    round_number INT NOT NULL,
    current_turn_order INT NOT NULL,
    created_at DATETIME NOT NULL,
    ended_at DATETIME,
    -- NULL unless the combat is ACTIVE: MySQL unique indexes treat every NULL as distinct,
    -- so this emulates Postgres' partial unique index (one active combat per session).
    active_session_id CHAR(36) GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN session_id ELSE NULL END) STORED,
    CONSTRAINT chk_combat_status CHECK (status IN ('ACTIVE', 'COMPLETED')),
    CONSTRAINT fk_combat_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id)
);

CREATE UNIQUE INDEX uq_combat_active_session ON combat (active_session_id);

CREATE TABLE combat_participant (
    id CHAR(36) PRIMARY KEY,
    combat_id CHAR(36) NOT NULL,
    team VARCHAR(20) NOT NULL,
    character_id CHAR(36),
    npc_id CHAR(36),
    name VARCHAR(120) NOT NULL,
    initiative INT NOT NULL,
    turn_order INT NOT NULL,
    actions_remaining INT NOT NULL,
    health_current INT NOT NULL,
    health_maximum INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_combat_participant_team CHECK (team IN ('PLAYER', 'ENEMY')),
    CONSTRAINT chk_combat_participant_status CHECK (status IN ('ACTIVE', 'DOWNED', 'DEAD')),
    CONSTRAINT chk_combat_participant_source CHECK (
        (character_id IS NOT NULL AND npc_id IS NULL) OR (character_id IS NULL AND npc_id IS NOT NULL)
    ),
    CONSTRAINT uq_combat_participant_turn_order UNIQUE (combat_id, turn_order),
    CONSTRAINT fk_combat_participant_combat
        FOREIGN KEY (combat_id)
        REFERENCES combat (id),
    CONSTRAINT fk_combat_participant_character
        FOREIGN KEY (character_id)
        REFERENCES player_character (id),
    CONSTRAINT fk_combat_participant_npc
        FOREIGN KEY (npc_id)
        REFERENCES npc (id)
);

CREATE INDEX ix_combat_participant_combat_id ON combat_participant (combat_id);
