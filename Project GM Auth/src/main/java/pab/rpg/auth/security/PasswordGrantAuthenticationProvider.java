package pab.rpg.auth.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.Set;

// Authenticates the resource owner (via the existing UserDetailsService/PasswordEncoder, same as the
// browser form-login) and mints tokens through Spring Authorization Server's normal token-generation
// pipeline (OAuth2TokenGenerator + OAuth2AuthorizationService) — so TokenCustomizerConfig's sub/aud
// override applies identically here, no matter the grant type.
public class PasswordGrantAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public PasswordGrantAuthenticationProvider(
            AuthenticationManager authenticationManager,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator
    ) {
        this.authenticationManager = authenticationManager;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordGrantAuthenticationToken passwordGrantAuthentication = (PasswordGrantAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(passwordGrantAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        Assert.notNull(registeredClient, "registeredClient cannot be null");

        if (!registeredClient.getAuthorizationGrantTypes().contains(PasswordGrantAuthenticationToken.PASSWORD)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        Authentication usernamePasswordAuthentication;
        try {
            usernamePasswordAuthentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            passwordGrantAuthentication.getUsername(), passwordGrantAuthentication.getPassword()));
        } catch (AuthenticationException ex) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, "Usuario o contraseña incorrectos.", null));
        }

        Set<String> authorizedScopes = registeredClient.getScopes();

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(usernamePasswordAuthentication)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(PasswordGrantAuthenticationToken.PASSWORD)
                .authorizationGrant(passwordGrantAuthentication);

        OAuth2TokenContext accessTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = tokenGenerator.generate(accessTokenContext);
        if (generatedAccessToken == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(usernamePasswordAuthentication.getName())
                .authorizationGrantType(PasswordGrantAuthenticationToken.PASSWORD)
                .authorizedScopes(authorizedScopes)
                // OAuth2RefreshTokenAuthenticationProvider reads this back later — without it, refreshing
                // fails with "principal cannot be null" (the built-in grants set it via their own internal helpers).
                .attribute(Principal.class.getName(), usernamePasswordAuthentication);

        OAuth2AccessToken accessToken = toAccessToken(authorizationBuilder, generatedAccessToken, accessTokenContext);

        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            OAuth2TokenContext refreshTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = tokenGenerator.generate(refreshTokenContext);
            if (generatedRefreshToken instanceof OAuth2RefreshToken token) {
                refreshToken = token;
                authorizationBuilder.refreshToken(refreshToken);
            }
        }

        authorizationService.save(authorizationBuilder.build());

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken clientPrincipal
                && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    // Mirrors the framework's own (package-private, so unreachable from here) OAuth2AuthenticationProviderUtils.accessToken().
    private static OAuth2AccessToken toAccessToken(
            OAuth2Authorization.Builder builder, OAuth2Token token, OAuth2TokenContext accessTokenContext) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                token.getTokenValue(),
                token.getIssuedAt(),
                token.getExpiresAt(),
                accessTokenContext.getAuthorizedScopes());
        builder.token(accessToken, metadata -> {
            if (token instanceof ClaimAccessor claimAccessor) {
                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims());
            }
        });
        return accessToken;
    }
}
