package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.session.ProcessedAction;
import pab.rpg.domain.repository.ProcessedActionRepository;
import pab.rpg.service.IdempotencyService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceImplTest {

    @Mock
    private ProcessedActionRepository processedActionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findExistingReturnsStoredResultWhenPresent() {
        IdempotencyServiceImpl service = new IdempotencyServiceImpl(processedActionRepository, objectMapper);
        UUID sessionId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        ProcessedAction stored = new ProcessedAction(
                UUID.randomUUID(), sessionId, idempotencyKey, actionId, Map.of("status", "RESOLVED"), Instant.now()
        );
        when(processedActionRepository.findBySessionIdAndIdempotencyKey(sessionId, idempotencyKey))
                .thenReturn(Optional.of(stored));

        Optional<IdempotencyService.StoredActionResult> result = service.findExisting(sessionId, idempotencyKey);

        assertTrue(result.isPresent());
        assertEquals(actionId, result.get().actionId());
    }

    @Test
    void recordPersistsProcessedAction() {
        IdempotencyServiceImpl service = new IdempotencyServiceImpl(processedActionRepository, objectMapper);
        UUID sessionId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();

        service.record(sessionId, idempotencyKey, actionId, Map.of("status", "RESOLVED"));

        verify(processedActionRepository).save(any(ProcessedAction.class));
    }
}
