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

public class CreateSessionSteps {

    private String characterName;
    private int characterLevel;
    private UUID playerId;
    private Response response;

    @Given("un nuevo personaje llamado {string} de nivel {int}")
    public void un_nuevo_personaje_llamado_de_nivel(String name, int level) {
        this.characterName = name;
        this.characterLevel = level;
        this.playerId = UUID.randomUUID();
    }

    @When("el jugador crea una sesión de juego en la localización {string}")
    public void el_jugador_crea_una_sesion_de_juego_en_la_localizacion(String locationCode) {
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

    @Then("la sesión se crea correctamente")
    public void la_sesion_se_crea_correctamente() {
        assertThat(response.statusCode(), equalTo(201));
    }

    @Then("la sesión pertenece al personaje {string}")
    public void la_sesion_pertenece_al_personaje(String expectedName) {
        response.then().body("character.name", equalTo(expectedName));
    }

    @Then("la localización actual de la sesión es {string}")
    public void la_localizacion_actual_de_la_sesion_es(String locationCode) {
        response.then().body("currentLocationId", equalTo(LocationCatalog.idOf(locationCode).toString()));
    }
}
