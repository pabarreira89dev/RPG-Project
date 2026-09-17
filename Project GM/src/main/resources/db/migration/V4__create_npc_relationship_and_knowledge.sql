CREATE TABLE npc (
    id UUID PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(120) NOT NULL,
    location_id UUID NOT NULL REFERENCES location (id),
    faction VARCHAR(60),
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ALIVE',
    CONSTRAINT uq_npc_code UNIQUE (code),
    CONSTRAINT chk_npc_status CHECK (status IN ('ALIVE', 'DEAD'))
);

INSERT INTO npc (id, code, name, location_id, faction, description, status) VALUES
    ('44444444-4444-4444-4444-444444444441', 'aron_tavernkeeper', 'Aron', '22222222-2222-2222-2222-222222222222',
     NULL, 'El tabernero, viudo y con deudas, conoce buena parte de los rumores de la aldea.', 'ALIVE'),
    ('44444444-4444-4444-4444-444444444442', 'village_guard', 'Guardia de la aldea', '11111111-1111-1111-1111-111111111111',
     NULL, 'Vigila la plaza y desconfía de los forasteros.', 'ALIVE'),
    ('44444444-4444-4444-4444-444444444443', 'village_elder', 'Anciana del pueblo', '11111111-1111-1111-1111-111111111111',
     NULL, 'Guarda la memoria histórica de la aldea.', 'ALIVE'),
    ('44444444-4444-4444-4444-444444444444', 'forest_hunter', 'Cazador del bosque', '33333333-3333-3333-3333-333333333333',
     NULL, 'Conoce los peligros recientes en el lindero del bosque.', 'ALIVE'),
    ('44444444-4444-4444-4444-444444444445', 'traveling_merchant', 'Mercader itinerante', '11111111-1111-1111-1111-111111111111',
     NULL, 'Pasa por la aldea vendiendo suministros y noticias de otras regiones.', 'ALIVE');

CREATE TABLE relationship (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES game_session (id),
    npc_id UUID NOT NULL REFERENCES npc (id),
    value INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_relationship_session_npc UNIQUE (session_id, npc_id)
);

CREATE TABLE npc_knowledge_fact (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES game_session (id),
    npc_id UUID NOT NULL REFERENCES npc (id),
    fact_key VARCHAR(100) NOT NULL,
    learned_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_npc_knowledge_fact UNIQUE (session_id, npc_id, fact_key)
);
