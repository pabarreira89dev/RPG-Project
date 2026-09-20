CREATE TABLE quest (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT uq_quest_code UNIQUE (code)
);

CREATE TABLE quest_stage (
    id CHAR(36) PRIMARY KEY,
    quest_id CHAR(36) NOT NULL,
    code VARCHAR(60) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_initial BOOLEAN NOT NULL DEFAULT FALSE,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_quest_stage_code UNIQUE (quest_id, code),
    CONSTRAINT fk_quest_stage_quest
        FOREIGN KEY (quest_id)
        REFERENCES quest (id)
);

CREATE TABLE quest_stage_transition (
    id CHAR(36) PRIMARY KEY,
    quest_id CHAR(36) NOT NULL,
    from_stage_id CHAR(36) NOT NULL,
    to_stage_id CHAR(36) NOT NULL,
    choice_key VARCHAR(60) NOT NULL,
    CONSTRAINT uq_quest_stage_transition UNIQUE (quest_id, from_stage_id, choice_key),
    CONSTRAINT fk_quest_stage_transition_quest
        FOREIGN KEY (quest_id)
        REFERENCES quest (id),
    CONSTRAINT fk_quest_stage_transition_from
        FOREIGN KEY (from_stage_id)
        REFERENCES quest_stage (id),
    CONSTRAINT fk_quest_stage_transition_to
        FOREIGN KEY (to_stage_id)
        REFERENCES quest_stage (id)
);

CREATE TABLE quest_state (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    quest_id CHAR(36) NOT NULL,
    current_stage_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uq_quest_state_session_quest UNIQUE (session_id, quest_id),
    CONSTRAINT chk_quest_state_status CHECK (status IN ('ACTIVE', 'COMPLETED')),
    CONSTRAINT fk_quest_state_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id),
    CONSTRAINT fk_quest_state_quest
        FOREIGN KEY (quest_id)
        REFERENCES quest (id),
    CONSTRAINT fk_quest_state_current_stage
        FOREIGN KEY (current_stage_id)
        REFERENCES quest_stage (id)
);

-- Quest 1: Aron's debt (tied to the aron_tavernkeeper NPC), branches into paying it off or confronting the creditor.
INSERT INTO quest (id, code, title, description) VALUES
    ('55555555-5555-5555-5555-555555555551', 'aron_debt', 'La deuda de Aron',
     'Aron, el tabernero, debe dinero a un prestamista y teme las consecuencias.');

INSERT INTO quest_stage (id, quest_id, code, description, is_initial, is_terminal) VALUES
    ('66666666-6666-6666-6666-666666666601', '55555555-5555-5555-5555-555555555551', 'quest_started',
     'Aron te confiesa que debe dinero a un prestamista y no sabe qué hacer.', TRUE, FALSE),
    ('66666666-6666-6666-6666-666666666602', '55555555-5555-5555-5555-555555555551', 'debt_paid',
     'Pagas la deuda de Aron de tu propio bolsillo. Su gratitud es evidente.', FALSE, TRUE),
    ('66666666-6666-6666-6666-666666666603', '55555555-5555-5555-5555-555555555551', 'creditor_confronted',
     'Te enfrentas al prestamista para que perdone la deuda de Aron.', FALSE, TRUE);

INSERT INTO quest_stage_transition (id, quest_id, from_stage_id, to_stage_id, choice_key) VALUES
    ('77777777-7777-7777-7777-777777777601', '55555555-5555-5555-5555-555555555551',
     '66666666-6666-6666-6666-666666666601', '66666666-6666-6666-6666-666666666602', 'pay'),
    ('77777777-7777-7777-7777-777777777602', '55555555-5555-5555-5555-555555555551',
     '66666666-6666-6666-6666-666666666601', '66666666-6666-6666-6666-666666666603', 'confront');

-- Quest 2: the threat at the forest edge (tied to the forest_hunter NPC), branches into investigating or ignoring it.
INSERT INTO quest (id, code, title, description) VALUES
    ('55555555-5555-5555-5555-555555555552', 'forest_threat', 'La amenaza del bosque',
     'El cazador ha visto algo peligroso en el lindero del bosque.');

INSERT INTO quest_stage (id, quest_id, code, description, is_initial, is_terminal) VALUES
    ('66666666-6666-6666-6666-666666666611', '55555555-5555-5555-5555-555555555552', 'quest_started',
     'El cazador te advierte de movimientos extraños en el lindero del bosque.', TRUE, FALSE),
    ('66666666-6666-6666-6666-666666666612', '55555555-5555-5555-5555-555555555552', 'threat_investigated',
     'Investigas el lindero y descubres el origen real de la amenaza.', FALSE, TRUE),
    ('66666666-6666-6666-6666-666666666613', '55555555-5555-5555-5555-555555555552', 'threat_ignored',
     'Decides no investigar. La amenaza queda sin resolver.', FALSE, TRUE);

INSERT INTO quest_stage_transition (id, quest_id, from_stage_id, to_stage_id, choice_key) VALUES
    ('77777777-7777-7777-7777-777777777611', '55555555-5555-5555-5555-555555555552',
     '66666666-6666-6666-6666-666666666611', '66666666-6666-6666-6666-666666666612', 'investigate'),
    ('77777777-7777-7777-7777-777777777612', '55555555-5555-5555-5555-555555555552',
     '66666666-6666-6666-6666-666666666611', '66666666-6666-6666-6666-666666666613', 'ignore');

-- Quest 3: the elder's history (tied to the village_elder NPC), branches into learning or dismissing it.
INSERT INTO quest (id, code, title, description) VALUES
    ('55555555-5555-5555-5555-555555555553', 'village_elder_history', 'La memoria de la anciana',
     'La anciana del pueblo guarda un recuerdo importante sobre el pasado de la aldea.');

INSERT INTO quest_stage (id, quest_id, code, description, is_initial, is_terminal) VALUES
    ('66666666-6666-6666-6666-666666666621', '55555555-5555-5555-5555-555555555553', 'quest_started',
     'La anciana insinúa que conoce un secreto sobre el pasado de la aldea.', TRUE, FALSE),
    ('66666666-6666-6666-6666-666666666622', '55555555-5555-5555-5555-555555555553', 'history_learned',
     'Escuchas con paciencia y la anciana te revela la historia completa.', FALSE, TRUE),
    ('66666666-6666-6666-6666-666666666623', '55555555-5555-5555-5555-555555555553', 'history_dismissed',
     'Decides no perder el tiempo y la anciana guarda silencio.', FALSE, TRUE);

INSERT INTO quest_stage_transition (id, quest_id, from_stage_id, to_stage_id, choice_key) VALUES
    ('77777777-7777-7777-7777-777777777621', '55555555-5555-5555-5555-555555555553',
     '66666666-6666-6666-6666-666666666621', '66666666-6666-6666-6666-666666666622', 'listen'),
    ('77777777-7777-7777-7777-777777777622', '55555555-5555-5555-5555-555555555553',
     '66666666-6666-6666-6666-666666666621', '66666666-6666-6666-6666-666666666623', 'dismiss');
