package pab.rpg.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Local/test convenience only (security.development-user-enabled=true): no real JWT is required.
// Identity comes from the X-Dev-Player-Id header if present (lets tests/E2E simulate several distinct
// players without a real identity provider), otherwise from a fixed configured dev player.
public class DevelopmentIdentityFilter extends OncePerRequestFilter {

    public static final String DEV_PLAYER_ID_HEADER = "X-Dev-Player-Id";

    private final UUID defaultPlayerId;

    public DevelopmentIdentityFilter(UUID defaultPlayerId) {
        this.defaultPlayerId = defaultPlayerId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        UUID playerId = resolvePlayerId(request.getHeader(DEV_PLAYER_ID_HEADER));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                playerId.toString(), null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private UUID resolvePlayerId(String header) {
        if (header == null || header.isBlank()) {
            return defaultPlayerId;
        }
        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            return defaultPlayerId;
        }
    }
}
