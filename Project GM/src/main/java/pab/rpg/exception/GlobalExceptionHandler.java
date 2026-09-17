package pab.rpg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleSessionNotFound(SessionNotFoundException exception) {
        return new ApiError("SESSION_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(StaleSessionVersionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleStaleSessionVersion(StaleSessionVersionException exception) {
        return new ApiError("STALE_SESSION_VERSION", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConcurrentModification(ObjectOptimisticLockingFailureException exception) {
        return new ApiError("STALE_SESSION_VERSION",
                "La partida ha cambiado. Actualiza el estado antes de repetir la acción.", Instant.now());
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiError handleActionNotAllowed(ActionNotAllowedException exception) {
        return new ApiError("ACTION_NOT_ALLOWED", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(QuestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleQuestNotFound(QuestNotFoundException exception) {
        return new ApiError("QUEST_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(QuestTransitionNotAllowedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiError handleQuestTransitionNotAllowed(QuestTransitionNotAllowedException exception) {
        return new ApiError("QUEST_TRANSITION_NOT_ALLOWED", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(CombatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCombatNotFound(CombatNotFoundException exception) {
        return new ApiError("COMBAT_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(CombatNotAllowedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiError handleCombatNotAllowed(CombatNotAllowedException exception) {
        return new ApiError("COMBAT_NOT_ALLOWED", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequest(RuntimeException exception) {
        return new ApiError("INVALID_REQUEST", exception.getMessage(), Instant.now());
    }

    public record ApiError(String code, String message, Instant timestamp) {
    }
}
