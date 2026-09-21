package com.pabarreira.tests;

import com.pabarreira.tests.support.ApiConfig;
import com.pabarreira.tests.support.AppLifecycle;
import com.pabarreira.tests.support.LocationCatalog;
import com.pabarreira.tests.support.NpcCatalog;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class GameSessionSteps {

    private String characterName;
    private int characterLevel;
    private UUID playerId;
    private UUID lastSessionId;
    private long lastSessionVersion;
    private UUID lastIdempotencyKey;
    private String firstActionId;
    private UUID lastCombatId;
    private String lastCombatStatus;
    private final Map<String, Integer> relationshipSnapshots = new HashMap<>();
    private Response response;

    @Given("un nuevo personaje llamado {string} de nivel {int}")
    public void un_nuevo_personaje_llamado_de_nivel(String name, int level) {
        this.characterName = name;
        this.characterLevel = level;
        this.playerId = UUID.randomUUID();
    }

    @Given("un personaje que ya existe")
    public void un_personaje_que_ya_existe() {
        this.characterName = "Aldric";
        this.characterLevel = 1;
        this.playerId = UUID.randomUUID();
    }

    @Given("un jugador cualquiera")
    public void un_jugador_cualquiera() {
        this.characterName = "Personaje de prueba";
        this.characterLevel = 1;
        this.playerId = UUID.randomUUID();
    }

    @Given("el jugador ha creado una sesión de juego en la localización {string}")
    public void el_jugador_ha_creado_una_sesion_de_juego_en_la_localizacion(String locationCode) {
        crearSesion(locationCode);
        lastSessionId = UUID.fromString(response.jsonPath().getString("sessionId"));
    }

    @Given("el jugador tiene una sesión de juego existente en la localización {string}")
    public void el_jugador_tiene_una_sesion_de_juego_existente_en_la_localizacion(String locationCode) {
        crearSesion(locationCode);
        assertThat(response.statusCode(), equalTo(201));
        lastSessionId = UUID.fromString(response.jsonPath().getString("sessionId"));
    }

    @When("el jugador crea una sesión de juego en la localización {string}")
    public void el_jugador_crea_una_sesion_de_juego_en_la_localizacion(String locationCode) {
        crearSesion(locationCode);
    }

    @When("el jugador consulta esa sesión")
    public void el_jugador_consulta_esa_sesion() {
        consultarSesion(lastSessionId, playerId);
    }

    @When("otro jugador distinto consulta esa sesión")
    public void otro_jugador_distinto_consulta_esa_sesion() {
        consultarSesion(lastSessionId, UUID.randomUUID());
    }

    @When("el jugador consulta una sesión con un identificador aleatorio")
    public void el_jugador_consulta_una_sesion_con_un_identificador_aleatorio() {
        consultarSesion(UUID.randomUUID(), playerId);
    }

    @When("el jugador lista sus sesiones")
    public void el_jugador_lista_sus_sesiones() {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        response = given()
                .header("X-Dev-Player-Id", playerId.toString())
                .when()
                .get("/api/v1/sessions");
    }

    @Then("la operación responde con estado {int}")
    public void la_operacion_responde_con_estado(int expectedStatus) {
        assertThat(response.statusCode(), equalTo(expectedStatus));
    }

    @Then("la sesión pertenece al personaje {string}")
    public void la_sesion_pertenece_al_personaje(String expectedName) {
        response.then().body("character.name", equalTo(expectedName));
    }

    @Then("la localización actual de la sesión es {string}")
    public void la_localizacion_actual_de_la_sesion_es(String locationCode) {
        response.then().body("currentLocationId", equalTo(LocationCatalog.idOf(locationCode).toString()));
    }

    @Then("el error devuelto tiene el código {string}")
    public void el_error_devuelto_tiene_el_codigo(String expectedCode) {
        response.then().body("code", equalTo(expectedCode));
    }

    @Then("la lista devuelta contiene {int} sesiones")
    public void la_lista_devuelta_contiene_sesiones(int expectedSize) {
        response.then().body("$", hasSize(expectedSize));
    }

    @Given("el jugador ha enviado la acción {string}")
    public void el_jugador_ha_enviado_la_accion(String text) {
        el_jugador_envia_la_accion(text);
    }

    @When("el jugador envía la acción {string}")
    public void el_jugador_envia_la_accion(String text) {
        lastIdempotencyKey = UUID.randomUUID();
        enviarAccion(text, lastIdempotencyKey, lastSessionVersion);
        if (response.statusCode() == 200) {
            firstActionId = response.jsonPath().getString("actionId");
        }
    }

    @When("el jugador repite la misma acción con la misma clave de idempotencia")
    public void el_jugador_repite_la_misma_accion_con_la_misma_clave_de_idempotencia() {
        enviarAccion("Miro a mi alrededor buscando algo interesante", lastIdempotencyKey, lastSessionVersion);
    }

    @When("el jugador envía una acción con una versión de sesión desfasada")
    public void el_jugador_envia_una_accion_con_una_version_de_sesion_desfasada() {
        enviarAccion("Miro a mi alrededor buscando algo interesante", UUID.randomUUID(), lastSessionVersion + 99);
    }

    @Then("la acción devuelve un resultado con tirada de dados")
    public void la_accion_devuelve_un_resultado_con_tirada_de_dados() {
        response.then()
                .body("result.roll.d20", both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(20)))
                .body("result.type", notNullValue());
    }

    @Then("la acción registra al menos un evento")
    public void la_accion_registra_al_menos_un_evento() {
        response.then().body("events", not(empty()));
    }

    @Then("la acción devuelve el mismo identificador que la primera vez")
    public void la_accion_devuelve_el_mismo_identificador_que_la_primera_vez() {
        assertThat(response.jsonPath().getString("actionId"), equalTo(firstActionId));
    }

    @Then("la acción registra un evento de cambio de relación")
    public void la_accion_registra_un_evento_de_cambio_de_relacion() {
        response.then().body("events", hasItem("RELATIONSHIP_CHANGED"));
    }

    private void enviarAccion(String text, UUID idempotencyKey, long expectedVersion) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Map<String, Object> body = Map.of(
                "text", text,
                "expectedVersion", expectedVersion,
                "idempotencyKey", idempotencyKey.toString()
        );

        response = given()
                .contentType("application/json")
                .header("X-Dev-Player-Id", playerId.toString())
                .body(body)
                .when()
                .post("/api/v1/sessions/{sessionId}/actions", lastSessionId.toString());
    }

    private void crearSesion(String locationCode) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Map<String, Object> body = Map.of(
                "worldId", UUID.randomUUID().toString(),
                "currentLocationId", LocationCatalog.idOf(locationCode).toString(),
                "worldTime", "2026-01-01T00:00:00Z",
                "characterName", characterName,
                "characterLevel", characterLevel,
                "characterExperience", 0,
                "attributes", Map.of(
                        "strength", 5,
                        "agility", 5,
                        "intellect", 5,
                        "willpower", 5,
                        "perception", 5,
                        "presence", 5
                ),
                "health", Map.of(
                        "maximum", 20,
                        "current", 20,
                        "wounds", 0
                )
        );

        response = given()
                .contentType("application/json")
                .header("X-Dev-Player-Id", playerId.toString())
                .body(body)
                .when()
                .post("/api/v1/sessions");

        if (response.statusCode() == 201) {
            lastSessionVersion = response.jsonPath().getLong("version");
        }
    }

    private void consultarSesion(UUID sessionId, UUID asPlayerId) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        response = given()
                .header("X-Dev-Player-Id", asPlayerId.toString())
                .when()
                .get("/api/v1/sessions/{sessionId}", sessionId.toString());
    }

    @Given("el jugador ha iniciado combate contra {string}")
    public void el_jugador_ha_iniciado_combate_contra(String npcCode) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Map<String, Object> body = Map.of("npcIds", List.of(NpcCatalog.idOf(npcCode).toString()));

        response = given()
                .contentType("application/json")
                .header("X-Dev-Player-Id", playerId.toString())
                .body(body)
                .when()
                .post("/api/v1/sessions/{sessionId}/combat/start", lastSessionId.toString());

        lastCombatId = UUID.fromString(response.jsonPath().getString("combatId"));
    }

    @When("el jugador ataca hasta terminar el combate")
    public void el_jugador_ataca_hasta_terminar_el_combate() {
        // Sin IA de enemigos: la suite simula el turno del bando ENEMY con IDs explícitos.
        for (int intento = 0; intento < 80; intento++) {
            String status = response.jsonPath().getString("status");
            if ("COMPLETED".equals(status)) {
                lastCombatStatus = status;
                return;
            }

            List<Map<String, Object>> participants = response.jsonPath().getList("participants");
            String currentParticipantId = response.jsonPath().getString("currentParticipantId");
            String currentTeam = participants.stream()
                    .filter(p -> currentParticipantId.equals(p.get("id")))
                    .map(p -> (String) p.get("team"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No se encontró al participante con el turno actual."));

            if ("PLAYER".equals(currentTeam)) {
                atacarComoJugador();
            } else {
                atacarComoEnemigo(participants, currentParticipantId);
            }
        }

        throw new IllegalStateException("El combate no terminó tras 80 intentos de ataque.");
    }

    @Then("el combate aparece como completado")
    public void el_combate_aparece_como_completado() {
        // No se puede releer /combat tras terminar (getActiveCombat da 404 sin combate ACTIVE);
        // se comprueba el estado capturado al terminar la pelea, no el `response` compartido (ya sobrescrito por pasos posteriores).
        assertThat(lastCombatStatus, equalTo("COMPLETED"));
    }

    private void atacarComoJugador() {
        Map<String, Object> body = Map.of("text", "Ataco a mi enemigo");
        atacar(body);
    }

    private void atacarComoEnemigo(List<Map<String, Object>> participants, String enemyParticipantId) {
        String playerParticipantId = participants.stream()
                .filter(p -> "PLAYER".equals(p.get("team")))
                .map(p -> (String) p.get("id"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró al participante del bando PLAYER."));

        Map<String, Object> body = Map.of(
                "attackerParticipantId", enemyParticipantId,
                "targetParticipantId", playerParticipantId
        );
        atacar(body);
    }

    private void atacar(Map<String, Object> body) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        response = given()
                .contentType("application/json")
                .header("X-Dev-Player-Id", playerId.toString())
                .body(body)
                .when()
                .post("/api/v1/sessions/{sessionId}/combat/{combatId}/attack", lastSessionId.toString(), lastCombatId.toString());
    }

    @Given("el jugador ha iniciado la misión {string}")
    public void el_jugador_ha_iniciado_la_mision(String questCode) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        response = given()
                .header("X-Dev-Player-Id", playerId.toString())
                .when()
                .post("/api/v1/sessions/{sessionId}/quests/{questCode}/start", lastSessionId.toString(), questCode);
    }

    @When("el jugador avanza la misión {string} con la decisión {string}")
    public void el_jugador_avanza_la_mision_con_la_decision(String questCode, String choiceKey) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Map<String, Object> body = Map.of("choiceKey", choiceKey);

        response = given()
                .contentType("application/json")
                .header("X-Dev-Player-Id", playerId.toString())
                .body(body)
                .when()
                .post("/api/v1/sessions/{sessionId}/quests/{questCode}/advance", lastSessionId.toString(), questCode);
    }

    @Then("la misión {string} sigue en estado {string}")
    public void la_mision_sigue_en_estado(String questCode, String expectedStatus) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Response quests = given()
                .header("X-Dev-Player-Id", playerId.toString())
                .when()
                .get("/api/v1/sessions/{sessionId}/quests", lastSessionId.toString());

        List<Map<String, Object>> questList = quests.jsonPath().getList("$");
        String actualStatus = questList.stream()
                .filter(q -> questCode.equals(q.get("questCode")))
                .map(q -> (String) q.get("status"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La misión '" + questCode + "' no aparece entre las misiones visibles."));

        assertThat(actualStatus, equalTo(expectedStatus));
    }

    @When("el jugador anota la relación actual con {string}")
    public void el_jugador_anota_la_relacion_actual_con(String npcCode) {
        relationshipSnapshots.put(npcCode, consultarRelacion(npcCode));
    }

    @Then("la relación con {string} sigue siendo la misma que antes del reinicio")
    public void la_relacion_con_sigue_siendo_la_misma_que_antes_del_reinicio(String npcCode) {
        Integer before = relationshipSnapshots.get(npcCode);
        assertThat("no se anotó la relación con '" + npcCode + "' antes del reinicio", before, notNullValue());
        assertThat(consultarRelacion(npcCode), equalTo(before));
    }

    private int consultarRelacion(String npcCode) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Response npcs = given()
                .header("X-Dev-Player-Id", playerId.toString())
                .when()
                .get("/api/v1/sessions/{sessionId}/npcs", lastSessionId.toString());

        List<Map<String, Object>> npcList = npcs.jsonPath().getList("$");
        return npcList.stream()
                .filter(npc -> npcCode.equals(npc.get("code")))
                .map(npc -> (Integer) npc.get("relationship"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El NPC '" + npcCode + "' no es visible en la localización actual."));
    }

    @When("se reinicia la aplicación")
    public void se_reinicia_la_aplicacion() {
        AppLifecycle.restart();
    }

    @When("el jugador consulta su sesión de nuevo")
    public void el_jugador_consulta_su_sesion_de_nuevo() {
        consultarSesion(lastSessionId, playerId);
    }
}
