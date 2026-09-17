package com.pabarreira.tests;

import com.pabarreira.tests.support.ApiConfig;
import com.pabarreira.tests.support.LocationCatalog;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class GameSessionSteps {

    private String characterName;
    private int characterLevel;
    private UUID playerId;
    private UUID lastSessionId;
    private Response response;

    @Given("un nuevo personaje llamado {string} de nivel {int}")
    public void un_nuevo_personaje_llamado_de_nivel(String name, int level) {
        this.characterName = name;
        this.characterLevel = level;
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
                .queryParam("playerId", playerId.toString())
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

    private void crearSesion(String locationCode) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        Map<String, Object> body = Map.of(
                "playerId", playerId.toString(),
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
                .body(body)
                .when()
                .post("/api/v1/sessions");
    }

    private void consultarSesion(UUID sessionId, UUID asPlayerId) {
        RestAssured.baseURI = ApiConfig.BASE_URI;

        response = given()
                .queryParam("playerId", asPlayerId.toString())
                .when()
                .get("/api/v1/sessions/{sessionId}", sessionId.toString());
    }
}
