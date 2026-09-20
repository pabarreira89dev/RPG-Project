CREATE TABLE location (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT uq_location_code UNIQUE (code)
);

INSERT INTO location (id, code, name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'village_square', 'Plaza de la Aldea',
     'El centro de la aldea, punto de encuentro y partida de la aventura.'),
    ('22222222-2222-2222-2222-222222222222', 'tavern', 'Taberna',
     'Un lugar concurrido donde circulan rumores y noticias de los alrededores.'),
    ('33333333-3333-3333-3333-333333333333', 'forest_edge', 'Lindero del Bosque',
     'El límite entre la aldea y el bosque cercano, origen de los problemas recientes.');

ALTER TABLE game_session
    ADD CONSTRAINT fk_game_session_current_location
        FOREIGN KEY (current_location_id)
        REFERENCES location (id);
