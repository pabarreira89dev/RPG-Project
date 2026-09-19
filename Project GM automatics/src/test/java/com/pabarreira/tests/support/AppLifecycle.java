package com.pabarreira.tests.support;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Apaga y relanza el proceso local de "Project GM" para que la suite E2E pueda comprobar que
 * el estado persistido en PostgreSQL sobrevive a un reinicio real de la aplicación.
 * Requiere que /actuator/shutdown esté habilitado (solo ocurre en el perfil "local", ver application-local.yml)
 * y que "mvn" esté disponible en el PATH.
 */
public final class AppLifecycle {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    private AppLifecycle() {
    }

    public static void restart() {
        shutdown();
        start();
        waitUntil(true, STARTUP_TIMEOUT, "arriba");
    }

    private static void shutdown() {
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URI + "/actuator/shutdown"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(
                    "No se pudo apagar la aplicación mediante /actuator/shutdown. "
                            + "Comprueba que 'Project GM' está corriendo en local con el endpoint de shutdown habilitado.", e);
        }
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "/actuator/shutdown devolvió " + response.statusCode() + ": " + response.body());
        }

        waitUntil(false, SHUTDOWN_TIMEOUT, "abajo");
        sleep(2000); // margen para que el SO libere el puerto antes de relanzar
    }

    private static void start() {
        File projectGmDir = projectGmDirectory();
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        List<String> command = windows
                ? List.of("cmd.exe", "/c", "mvn spring-boot:run")
                : List.of("sh", "-c", "mvn spring-boot:run");

        try {
            new ProcessBuilder(command)
                    .directory(projectGmDir)
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile(projectGmDir)))
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo relanzar 'Project GM' con 'mvn spring-boot:run'.", e);
        }
    }

    private static File projectGmDirectory() {
        File dir = new File(System.getProperty("user.dir"), "../Project GM");
        if (!new File(dir, "pom.xml").isFile()) {
            throw new IllegalStateException(
                    "No se encontró 'Project GM/pom.xml' junto a este proyecto (resuelto como " + dir.getAbsolutePath() + ").");
        }
        return dir;
    }

    private static File logFile(File projectGmDir) {
        File targetDir = new File(projectGmDir, "target");
        targetDir.mkdirs();
        return new File(targetDir, "e2e-restart.log");
    }

    private static void waitUntil(boolean expectUp, Duration timeout, String estadoEsperado) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (isUp() == expectUp) {
                return;
            }
            sleep(1000);
        }
        throw new IllegalStateException("Timeout esperando a que 'Project GM' quedara " + estadoEsperado + ".");
    }

    private static boolean isUp() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URI + "/actuator/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
