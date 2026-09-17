CREATE TABLE quest (
    id UUID PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT uq_quest_code UNIQUE (code)
);

CREATE TABLE quest_stage (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL REFERENCES quest (id),
    code VARCHAR(60) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_initial BOOLEAN NOT NULL DEFAULT FALSE,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_quest_stage_code UNIQUE (quest_id, code)
);

CREATE TABLE quest_stage_transition (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL REFERENCES quest (id),
    from_stage_id UUID NOT NULL REFERENCES quest_stage (id),
    to_stage_id UUID NOT NULL REFERENCES quest_stage (id),
    choice_key VARCHAR(60) NOT NULL,
    CONSTRAINT uq_quest_stage_transition UNIQUE (quest_id, from_stage_id, choice_key)
);

CREATE TABLE quest_state (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES game_session (id),
    quest_id UUID NOT NULL REFERENCES quest (id),
    current_stage_id UUID NOT NULL REFERENCES quest_stage (id),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_quest_state_session_quest UNIQUE (session_id, quest_id),
    CONSTRAINT chk_quest_state_status CHECK (status IN ('ACTIVE', 'COMPLETED'))
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
