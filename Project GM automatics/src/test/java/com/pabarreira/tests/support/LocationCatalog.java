package com.pabarreira.tests.support;

import java.util.Map;
import java.util.UUID;

/** IDs fijos de las localizaciones seed (ver V3__create_location.sql en Project GM). */
public final class LocationCatalog {

    private static final Map<String, UUID> IDS_BY_CODE = Map.of(
            "village_square", UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "tavern", UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "forest_edge", UUID.fromString("33333333-3333-3333-3333-333333333333")
    );

    private LocationCatalog() {
    }

    public static UUID idOf(String code) {
        UUID id = IDS_BY_CODE.get(code);
        if (id == null) {
            throw new IllegalArgumentException("Localización desconocida: " + code);
        }
        return id;
    }
}
