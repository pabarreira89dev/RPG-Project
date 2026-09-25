package pab.rpg.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;

// Signing key for issued JWTs. In cloud, loaded from PEM env vars (PKCS8 private / X.509 public,
// e.g. produced by "openssl genpkey -algorithm RSA" + "openssl rsa -pubout") so tokens survive restarts.
// In local, generated once per boot: acceptable since the in-memory OAuth2AuthorizationService (see
// AuthorizationServerConfig) already invalidates outstanding sessions on every restart anyway.
@Configuration
@RequiredArgsConstructor
public class JwkConfig {

    private final AuthProperties authProperties;

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        RSAKey rsaKey = isConfiguredKeyPresent()
                ? loadConfiguredKey()
                : generateEphemeralKey();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private boolean isConfiguredKeyPresent() {
        return authProperties.jwkPrivateKeyPem() != null && authProperties.jwkPublicKeyPem() != null;
    }

    private RSAKey loadConfiguredKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(decodePem(authProperties.jwkPublicKeyPem())));
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(decodePem(authProperties.jwkPrivateKeyPem())));

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId(publicKey))
                .build();
    }

    private RSAKey generateEphemeralKey() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        return new RSAKey.Builder(publicKey)
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(keyId(publicKey))
                .build();
    }

    private static byte[] decodePem(String pem) {
        String cleaned = pem
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }

    private static String keyId(RSAPublicKey publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getModulus().toByteArray());
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
