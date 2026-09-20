package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.entity.ProcessedAction;
import pab.rpg.domain.repository.ProcessedActionRepository;
import pab.rpg.service.IdempotencyService;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final Logger LOG = LoggerFactory.getLogger(IdempotencyServiceImpl.class);

    private final ProcessedActionRepository processedActionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredActionResult> findExisting(UUID sessionId, UUID idempotencyKey) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");

        LOG.info("Retrieving processed action for sessionId={} and idempotencyKey={}", sessionId, idempotencyKey);

        return processedActionRepository.findBySessionIdAndIdempotencyKey(sessionId, idempotencyKey)
                .map(action -> new StoredActionResult(action.getActionId(), action.getResponsePayload()));
    }

    @Override
    public void record(UUID sessionId, UUID idempotencyKey, UUID actionId, Object responsePayload) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        Objects.requireNonNull(actionId, "actionId must not be null");
        Objects.requireNonNull(responsePayload, "responsePayload must not be null");

        ProcessedAction action = new ProcessedAction(
                null,
                sessionId,
                idempotencyKey,
                actionId,
                toPayloadMap(responsePayload),
                Instant.now()
        );

        LOG.info("Recording processed action for sessionId={} and idempotencyKey={}", sessionId, idempotencyKey);
        processedActionRepository.save(action);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toPayloadMap(Object payload) {
        return objectMapper.convertValue(payload, Map.class);
    }
}
