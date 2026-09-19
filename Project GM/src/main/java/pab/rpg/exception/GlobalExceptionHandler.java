package pab.rpg.exception;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pab.rpg.config.CorrelationIdFilter;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleSessionNotFound(SessionNotFoundException exception) {
        return apiError("SESSION_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(StaleSessionVersionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleStaleSessionVersion(StaleSessionVersionException exception) {
        meterRegistry.counter("pab.rpg.session.version.conflicts").increment();
        return apiError("STALE_SESSION_VERSION", exception.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConcurrentModification(ObjectOptimisticLockingFailureException exception) {
        meterRegistry.counter("pab.rpg.session.version.conflicts").increment();
        return apiError("STALE_SESSION_VERSION",
                "La partida ha cambiado. Actualiza el estado antes de repetir la acción.");
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiError handleActionNotAllowed(ActionNotAllowedException exception) {
        return apiError("ACTION_NOT_ALLOWED", exception.getMessage());
    }

    @ExceptionHandler(QuestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleQuestNotFound(QuestNotFoundException exception) {
        return apiError("QUEST_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(QuestTransitionNotAllowedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiError handleQuestTransitionNotAllowed(QuestTransitionNotAllowedException exception) {
        return apiError("QUEST_TRANSITION_NOT_ALLOWED", exception.getMessage());
    }

    @ExceptionHandler(CombatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCombatNotFound(CombatNotFoundException exception) {
        return apiError("COMBAT_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(CombatNotAllowedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiError handleCombatNotAllowed(CombatNotAllowedException exception) {
        return apiError("COMBAT_NOT_ALLOWED", exception.getMessage());
    }

    @ExceptionHandler(AiUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleAiUnavailable(AiUnavailableException exception) {
        return apiError("OPENAI_UNAVAILABLE", exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequest(RuntimeException exception) {
        meterRegistry.counter("pab.rpg.validation.errors").increment();
        return apiError("INVALID_REQUEST", exception.getMessage());
    }

    private ApiError apiError(String code, String message) {
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        return new ApiError(code, message, Instant.now(), correlationId);
    }

    public record ApiError(String code, String message, Instant timestamp, String correlationId) {
    }
}
