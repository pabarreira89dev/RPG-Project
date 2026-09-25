package pab.rpg.auth.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import pab.rpg.auth.security.PasswordGrantAuthenticationConverter;
import pab.rpg.auth.security.PasswordGrantAuthenticationProvider;
import pab.rpg.auth.security.PasswordGrantAuthenticationToken;

import java.time.Duration;
import java.util.UUID;

// Wires Spring Authorization Server's endpoints — the only registered client is the Android app
// (public, no secret), and the only grants it's allowed are the custom "password" grant (native
// username/password form, no browser — see PasswordGrantAuthenticationProvider) and "refresh_token".
@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final AuthProperties authProperties;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<OAuth2Token> tokenGenerator
    ) throws Exception {
        // Matches Boot's own OAuth2AuthorizationServerWebSecurityConfiguration DSL usage: the lambda closes
        // over the outer "http" to set the security matcher from the configurer it receives.
        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            // Project GM (resource server) resolves this issuer via JwtDecoders.fromIssuerLocation, which
            // looks up "/.well-known/openid-configuration" first — without oidc() enabled here that path
            // isn't registered and falls through to the default filter chain's login page instead of 404,
            // which JwtDecoders can't tell apart from "not found", so it needs to actually exist.
            authorizationServer.oidc(Customizer.withDefaults());
            authorizationServer.tokenEndpoint(tokenEndpoint -> tokenEndpoint
                    .accessTokenRequestConverter(new PasswordGrantAuthenticationConverter())
                    .authenticationProvider(new PasswordGrantAuthenticationProvider(
                            authenticationManager, authorizationService, tokenGenerator)));
        });
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
        // No login page of any kind exists in this service — an unauthenticated request to any AS endpoint
        // (e.g. the unused /oauth2/authorize) just gets a plain 401, never an HTML redirect.
        http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient androidClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(authProperties.androidClientId())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientSecret(passwordEncoder.encode(authProperties.androidClientSecret()))
                .authorizationGrantType(PasswordGrantAuthenticationToken.PASSWORD)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("game.api")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(authProperties.accessTokenTtlMinutes()))
                        .refreshTokenTimeToLive(Duration.ofDays(authProperties.refreshTokenTtlDays()))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(androidClient);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(authProperties.issuerUri())
                .build();
    }

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    // Explicit (rather than the framework's auto-composed default) so PasswordGrantAuthenticationProvider
    // can share this exact instance — both it and the built-in grants (refresh_token, etc.) must produce
    // tokens through the same generator for TokenCustomizerConfig's sub/aud override to apply everywhere.
    @Bean
    public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(
            JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer
    ) {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer);
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, new OAuth2AccessTokenGenerator(), new OAuth2RefreshTokenGenerator());
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
