package pab.rpg.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Parses "grant_type=password&username=...&password=..." from the token endpoint request body —
// the only non-standard piece of the request shape; everything else (client auth, token issuance,
// storage) is Spring Authorization Server's normal machinery, untouched.
public class PasswordGrantAuthenticationConverter implements AuthenticationConverter {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!PasswordGrantAuthenticationToken.PASSWORD.getValue().equals(grantType)) {
            return null;
        }

        MultiValueMap<String, String> parameters = formParameters(request);

        String username = parameters.getFirst(USERNAME);
        String password = parameters.getFirst(PASSWORD);
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "username and password are required", null));
        }

        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        Set<String> requestedScopes = StringUtils.hasText(scope)
                ? new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")))
                : null;

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, values) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) && !key.equals(USERNAME) && !key.equals(PASSWORD)
                    && !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, values.size() == 1 ? values.get(0) : values.toArray(new String[0]));
            }
        });

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        return new PasswordGrantAuthenticationToken(username, password, clientPrincipal, requestedScopes, additionalParameters);
    }

    private static MultiValueMap<String, String> formParameters(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                parameters.add(key, value);
            }
        });
        return parameters;
    }
}
