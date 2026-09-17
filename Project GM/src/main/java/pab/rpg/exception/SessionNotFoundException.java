package pab.rpg.exception;

import java.util.UUID;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(UUID sessionId, UUID playerId) {
        super("Game session " + sessionId + " was not found for player " + playerId);
    }
}
