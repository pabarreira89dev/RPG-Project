package pab.rpg.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TDD MVP v0.2 §14: every request carries a correlationId (echoed back to the client) and, when the
// URL contains one, a sessionId. Both are put in MDC so logback includes them in every structured log line.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String SESSION_ID_MDC_KEY = "sessionId";

    private static final Pattern SESSION_ID_IN_PATH = Pattern.compile(
            "/sessions/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER))
                .filter(header -> !header.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        try {
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            extractSessionId(request.getRequestURI()).ifPresent(sessionId -> MDC.put(SESSION_ID_MDC_KEY, sessionId));
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
            MDC.remove(SESSION_ID_MDC_KEY);
        }
    }

    private Optional<String> extractSessionId(String requestUri) {
        Matcher matcher = SESSION_ID_IN_PATH.matcher(requestUri);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
