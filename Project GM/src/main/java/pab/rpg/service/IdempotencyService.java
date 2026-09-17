package pab.rpg.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyService {

    Optional<StoredActionResult> findExisting(UUID sessionId, UUID idempotencyKey);

    void record(UUID sessionId, UUID idempotencyKey, UUID actionId, Object responsePayload);

    record StoredActionResult(UUID actionId, Map<String, Object> responsePayload) {
    }
}
