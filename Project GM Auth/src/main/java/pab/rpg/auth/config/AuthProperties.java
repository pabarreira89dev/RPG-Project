package pab.rpg.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// Bound from auth.* (see application-local.yml/application-cloud.yml). issuerUri/audience must be
// copy-pasted into Project GM's JWT_ISSUER_URI/JWT_AUDIENCE cloud env vars — see SecurityConfig there.
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        String issuerUri,
        String audience,
        String androidClientId,
        // Spring Authorization Server's only "no secret" client auth mechanism (PublicClientAuthenticationConverter)
        // is hardcoded to the authorization_code+PKCE grant; the custom "password" grant needs a real client
        // credential instead. Not a true secret (it ships inside the APK, extractable by reverse-engineering) —
        // it gates casual/accidental direct token-endpoint calls, not a determined attacker.
        String androidClientSecret,
        @DefaultValue("30") long accessTokenTtlMinutes,
        @DefaultValue("30") long refreshTokenTtlDays,
        // Left null in local (an ephemeral keypair is generated instead, see JwkConfig); required in cloud.
        String jwkPrivateKeyPem,
        String jwkPublicKeyPem
) {
}
