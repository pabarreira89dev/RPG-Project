package com.pabarreira.tests.support;

import java.util.Map;
import java.util.UUID;

/** IDs fijos de los NPCs seed (ver V4__create_npc_relationship_and_knowledge.sql en Project GM). */
public final class NpcCatalog {

    private static final Map<String, UUID> IDS_BY_CODE = Map.of(
            "aron_tavernkeeper", UUID.fromString("44444444-4444-4444-4444-444444444441"),
            "village_guard", UUID.fromString("44444444-4444-4444-4444-444444444442"),
            "village_elder", UUID.fromString("44444444-4444-4444-4444-444444444443"),
            "forest_hunter", UUID.fromString("44444444-4444-4444-4444-444444444444"),
            "traveling_merchant", UUID.fromString("44444444-4444-4444-4444-444444444445")
    );

    private NpcCatalog() {
    }

    public static UUID idOf(String code) {
        UUID id = IDS_BY_CODE.get(code);
        if (id == null) {
            throw new IllegalArgumentException("NPC desconocido: " + code);
        }
        return id;
    }
}
