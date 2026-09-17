package pab.rpg.exception;

import java.util.UUID;

public class StaleSessionVersionException extends RuntimeException {

    public StaleSessionVersionException(UUID sessionId, long expectedVersion, long actualVersion) {
        super("Game session " + sessionId + " version mismatch: expected " + expectedVersion + " but was " + actualVersion);
    }
}
