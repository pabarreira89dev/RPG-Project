package pab.rpg.auth.security;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Custom OAuth2 grant so the Android app can log in with a native username/password form and never
// open a browser — Spring Authorization Server deliberately ships no "password" grant out of the box
// (OAuth 2.1 discourages it for third-party clients), but it's a standard extension point for a
// first-party client the product itself controls end to end (see AuthorizationServerConfig).
@Getter
public class PasswordGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");

    private final String username;
    private final String password;
    private final Set<String> scopes;

    public PasswordGrantAuthenticationToken(
            String username,
            String password,
            Authentication clientPrincipal,
            Set<String> scopes,
            Map<String, Object> additionalParameters
    ) {
        super(PASSWORD, clientPrincipal, additionalParameters);
        Assert.hasText(username, "username cannot be empty");
        Assert.hasText(password, "password cannot be empty");
        this.username = username;
        this.password = password;
        this.scopes = Collections.unmodifiableSet(scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }
}
