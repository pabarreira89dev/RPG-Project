CREATE TABLE item_template (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT uq_item_template_code UNIQUE (code)
);

INSERT INTO item_template (id, code, name, description) VALUES
    ('55555555-5555-5555-5555-555555555551', 'rusty_dagger', 'Daga oxidada',
     'Una daga vieja y desgastada, todavía sirve para defenderse.'),
    ('55555555-5555-5555-5555-555555555552', 'healing_herbs', 'Hierbas curativas',
     'Un manojo de hierbas conocidas por sus propiedades curativas.'),
    ('55555555-5555-5555-5555-555555555553', 'old_coin_pouch', 'Bolsa de monedas antiguas',
     'Una bolsa de cuero con algunas monedas antiguas dentro.');

CREATE TABLE item (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    template_id CHAR(36) NOT NULL,
    owner_id CHAR(36),
    location_id CHAR(36),
    quantity INT NOT NULL DEFAULT 1,
    durability INT,
    CONSTRAINT chk_item_owner_or_location CHECK (
        (owner_id IS NOT NULL AND location_id IS NULL) OR (owner_id IS NULL AND location_id IS NOT NULL)
    ),
    CONSTRAINT fk_item_session
        FOREIGN KEY (session_id)
        REFERENCES game_session (id),
    CONSTRAINT fk_item_template
        FOREIGN KEY (template_id)
        REFERENCES item_template (id),
    CONSTRAINT fk_item_character
        FOREIGN KEY (owner_id)
        REFERENCES player_character (id),
    CONSTRAINT fk_item_location
        FOREIGN KEY (location_id)
        REFERENCES location (id)
);

CREATE INDEX ix_item_session_id ON item (session_id);
