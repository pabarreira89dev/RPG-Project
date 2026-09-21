package pab.rpg.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

// development-user-enabled must be false everywhere the app is reachable from outside localhost (TDD MVP v0.2 §12).
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        boolean developmentUserEnabled,
        UUID developmentPlayerId,
        String jwtIssuerUri,
        String jwtAudience
) {
}
