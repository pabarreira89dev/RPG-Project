package pab.rpg.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import pab.rpg.auth.domain.repository.AppUserRepository;

import java.util.List;

// Single point that ties this service's issued tokens to Project GM's JWT contract
// (Project GM/src/main/java/pab/rpg/security/SecurityConfig.java): "sub" must be the account's own
// UUID (not the username Spring Security uses internally as the principal name), and "aud" must be
// present and match Project GM's configured audience.
@Configuration
@RequiredArgsConstructor
public class TokenCustomizerConfig {

    private final AppUserRepository appUserRepository;
    private final AuthProperties authProperties;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                String username = context.getPrincipal().getName();
                appUserRepository.findByUsername(username).ifPresent(appUser ->
                        context.getClaims()
                                .subject(appUser.getId().toString())
                                .claim("preferred_username", appUser.getUsername())
                                .audience(List.of(authProperties.audience())));
            }
        };
    }
}
