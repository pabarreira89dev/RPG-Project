package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.api.dto.SubmitActionCommand;
import pab.rpg.api.dto.response.ActionResponse;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.world.Location;
import pab.rpg.domain.session.SessionStatus;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.domain.rules.ActionType;
import pab.rpg.domain.rules.CheckResolution;
import pab.rpg.domain.rules.CheckResolver;
import pab.rpg.domain.rules.Difficulty;
import pab.rpg.domain.rules.ResultGrade;
import pab.rpg.exception.StaleSessionVersionException;
import pab.rpg.service.GameEventService;
import pab.rpg.service.GameSessionService;
import pab.rpg.service.IdempotencyService;
import pab.rpg.service.MasterAdapter;
import pab.rpg.service.NpcService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionServiceImplTest {

    @Mock
    private GameSessionService gameSessionService;
    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private GameEventService gameEventService;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private CheckResolver checkResolver;
    @Mock
    private NpcService npcService;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private MasterAdapter masterAdapter;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private ActionServiceImpl service() {
        return new ActionServiceImpl(
                gameSessionService, gameSessionRepository, gameEventService, idempotencyService, checkResolver,
                List.of(), npcService, objectMapper, locationRepository, masterAdapter, meterRegistry
        );
    }

    private GameSession session(long version) {
        Character character = new Character(
                UUID.randomUUID(), "Aren", 1, 0,
                new AttributeSet(10, 10, 14, 10, 10, 10),
                new HealthState(10, 10, 0)
        );
        return new GameSession(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), version, character
        );
    }

    @Test
    void throwsWhenExpectedVersionIsStale() {
        GameSession session = session(5L);
        when(gameSessionService.getSession(any(), any())).thenReturn(session);
        when(idempotencyService.findExisting(any(), any())).thenReturn(Optional.empty());

        SubmitActionCommand command =
                new SubmitActionCommand(session.getId(), session.getPlayerId(), "Miro alrededor.", ActionType.INVESTIGATION, null, 4L, UUID.randomUUID());

        assertThrows(StaleSessionVersionException.class, () -> service().submitAction(command));
        verify(gameEventService, never()).append(any(), any(), any(), any(), any());
    }

    @Test
    void returnsStoredResponseWhenIdempotencyKeyAlreadyProcessed() {
        GameSession session = session(5L);
        UUID idempotencyKey = UUID.randomUUID();
        ActionResponse stored = new ActionResponse(
                UUID.randomUUID(), "RESOLVED", "Ya resuelto.",
                new ActionResponse.ResultDetails("EXITO", new ActionResponse.RollDetails(12, 3, 15, 14)),
                List.of("ACTION_RESOLVED"), 6L
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> storedPayload = objectMapper.convertValue(stored, Map.class);

        when(gameSessionService.getSession(any(), any())).thenReturn(session);
        when(idempotencyService.findExisting(session.getId(), idempotencyKey))
                .thenReturn(Optional.of(new IdempotencyService.StoredActionResult(stored.actionId(), storedPayload)));

        SubmitActionCommand command =
                new SubmitActionCommand(session.getId(), session.getPlayerId(), "Miro alrededor.", ActionType.INVESTIGATION, null, 5L, idempotencyKey);

        ActionResponse result = service().submitAction(command);

        assertEquals(stored, result);
        verify(gameSessionRepository, never()).saveAndFlush(any());
        verify(gameEventService, never()).append(any(), any(), any(), any(), any());
    }

    @Test
    void resolvesActionAndAppendsEvent() {
        GameSession session = session(5L);
        when(gameSessionService.getSession(any(), any())).thenReturn(session);
        when(idempotencyService.findExisting(any(), any())).thenReturn(Optional.empty());
        when(gameSessionRepository.saveAndFlush(any())).thenReturn(session);
        when(checkResolver.resolve(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(new CheckResolution(123L, 12, 2, 0, 0, 14, Difficulty.MODERATE, 0, ResultGrade.EXITO));
        when(locationRepository.findById(session.getCurrentLocationId()))
                .thenReturn(Optional.of(new Location(session.getCurrentLocationId(), "village_square", "Plaza", "Una plaza tranquila.")));
        when(masterAdapter.narrate(any())).thenReturn("Tu intento sale bien.");

        SubmitActionCommand command =
                new SubmitActionCommand(session.getId(), session.getPlayerId(), "Miro alrededor.", ActionType.INVESTIGATION, null, 5L, UUID.randomUUID());

        ActionResponse response = service().submitAction(command);

        assertEquals("RESOLVED", response.status());
        assertEquals("EXITO", response.result().type());
        assertEquals(14, response.result().roll().total());
        assertEquals(session.getVersion(), response.stateVersion());
        verify(gameEventService, times(1)).append(any(), eq("ACTION_RESOLVED"), any(), any(), any());
        verify(idempotencyService, times(1)).record(any(), any(), any(), any());
    }

    @Test
    void resolvesActionUsingInterpretedActionTypeWhenOmitted() {
        GameSession session = session(5L);
        when(gameSessionService.getSession(any(), any())).thenReturn(session);
        when(idempotencyService.findExisting(any(), any())).thenReturn(Optional.empty());
        when(gameSessionRepository.saveAndFlush(any())).thenReturn(session);
        when(checkResolver.resolve(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(new CheckResolution(123L, 12, 2, 0, 0, 14, Difficulty.MODERATE, 0, ResultGrade.EXITO));
        when(locationRepository.findById(session.getCurrentLocationId()))
                .thenReturn(Optional.of(new Location(session.getCurrentLocationId(), "village_square", "Plaza", "Una plaza tranquila.")));
        when(npcService.getNpcsAtLocation(session.getCurrentLocationId())).thenReturn(List.of());
        when(masterAdapter.interpret(any()))
                .thenReturn(new MasterAdapter.ActionIntent(ActionType.INVESTIGATION, null));
        when(masterAdapter.narrate(any())).thenReturn("Tu intento sale bien.");

        SubmitActionCommand command =
                new SubmitActionCommand(session.getId(), session.getPlayerId(), "Busco pistas.", null, null, 5L, UUID.randomUUID());

        ActionResponse response = service().submitAction(command);

        assertEquals("RESOLVED", response.status());
        verify(masterAdapter, times(1)).interpret(any());
        verify(gameEventService, times(1)).append(any(), eq("ACTION_RESOLVED"), any(), any(), any());
    }

    @Test
    void resolvesSocialActionWithTargetNpcAndChangesRelationship() {
        GameSession session = session(5L);
        UUID npcId = UUID.randomUUID();
        when(gameSessionService.getSession(any(), any())).thenReturn(session);
        when(idempotencyService.findExisting(any(), any())).thenReturn(Optional.empty());
        when(gameSessionRepository.saveAndFlush(any())).thenReturn(session);
        when(checkResolver.resolve(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(new CheckResolution(123L, 12, 2, 0, 0, 14, Difficulty.MODERATE, 0, ResultGrade.EXITO));
        when(npcService.changeRelationship(session.getId(), npcId, 1)).thenReturn(4);
        when(locationRepository.findById(session.getCurrentLocationId()))
                .thenReturn(Optional.of(new Location(session.getCurrentLocationId(), "village_square", "Plaza", "Una plaza tranquila.")));
        when(masterAdapter.narrate(any())).thenReturn("Lo consigues.");

        SubmitActionCommand command =
                new SubmitActionCommand(session.getId(), session.getPlayerId(), "Convenzo al guardia.", ActionType.SOCIAL, npcId, 5L, UUID.randomUUID());

        ActionResponse response = service().submitAction(command);

        assertEquals(List.of("ACTION_RESOLVED", "RELATIONSHIP_CHANGED"), response.events());
        verify(npcService, times(1)).changeRelationship(session.getId(), npcId, 1);
        verify(gameEventService, times(1)).append(any(), eq("RELATIONSHIP_CHANGED"), any(), any(), any());
    }
}
