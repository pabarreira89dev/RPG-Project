package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.api.dto.SubmitActionCommand;
import pab.rpg.api.dto.response.ActionResponse;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.world.Location;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.domain.rules.*;
import pab.rpg.exception.ActionNotAllowedException;
import pab.rpg.exception.StaleSessionVersionException;
import pab.rpg.service.*;
import pab.rpg.service.MasterAdapter.ActionIntent;
import pab.rpg.service.MasterAdapter.InterpretationRequest;
import pab.rpg.service.MasterAdapter.NarrationRequest;
import pab.rpg.service.MasterAdapter.VisibleNpc;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ActionServiceImpl implements ActionService {

    private static final Logger LOG = LoggerFactory.getLogger(ActionServiceImpl.class);

    private static final Duration ACTION_DURATION = Duration.ofMinutes(10);
    private static final int MAX_TEXT_LENGTH = 2000;

    private final GameSessionService gameSessionService;
    private final GameSessionRepository gameSessionRepository;
    private final GameEventService gameEventService;
    private final IdempotencyService idempotencyService;
    private final CheckResolver checkResolver;
    private final List<GameRule> gameRules;
    private final NpcService npcService;
    private final ObjectMapper objectMapper;
    private final LocationRepository locationRepository;
    private final MasterAdapter masterAdapter;
    private final ConversationMemoryService conversationMemoryService;
    private final MeterRegistry meterRegistry;

    @Override
    public ActionResponse submitAction(SubmitActionCommand command) {
        validate(command);

        LOG.info("Submitting action for sessionId={} playerId={} text={}",
                command.sessionId(),
                command.playerId(),
                command.text()
        );

        Timer.Sample sample = Timer.start(meterRegistry);
        boolean success = false;
        try {
            ActionResponse response = resolveAction(command);
            success = true;
            LOG.info("Action resolved for sessionId={} playerId={} narration={}",
                    command.sessionId(),
                    command.playerId(),
                    response.narration()
            );
            return response;
        } finally {
            sample.stop(meterRegistry.timer("pab.rpg.action.duration", "outcome", success ? "success" : "error"));
        }
    }

    private ActionResponse resolveAction(SubmitActionCommand command) {
        GameSession session = gameSessionService.getSession(command.sessionId(), command.playerId());

        Optional<IdempotencyService.StoredActionResult> existing =
                idempotencyService.findExisting(command.sessionId(), command.idempotencyKey());
        if (existing.isPresent()) {
            LOG.info("Found existing processed action for sessionId={} idempotencyKey={}",
                    command.sessionId(),
                    command.idempotencyKey());
            return objectMapper.convertValue(existing.get().responsePayload(), ActionResponse.class);
        }

        if (session.getVersion() != command.expectedVersion()) {
            LOG.error("Stale session version for sessionId={} playerId={}: expected={}, actual={}",
                    command.sessionId(),
                    command.playerId(),
                    command.expectedVersion(),
                    session.getVersion()
            );
            throw new StaleSessionVersionException(command.sessionId(), command.expectedVersion(), session.getVersion());
        }

        ActionType actionType = command.actionType();
        UUID targetNpcId = command.targetNpcId();
        String recentConversation = conversationMemoryService.summarizeRecent(session.getId());
        if (actionType == null) {
            ActionIntent intent = masterAdapter.interpret(new InterpretationRequest(
                    sceneSummary(session.getCurrentLocationId()),
                    command.text(),
                    visibleNpcs(session.getCurrentLocationId()),
                    recentConversation
            ));
            actionType = intent.actionType();
            if (targetNpcId == null) {
                targetNpcId = intent.targetNpcId();
            }
        }

        ActionContext context = new ActionContext(session, actionType, targetNpcId);
        gameRules.forEach(rule -> rule.check(context));
        meterRegistry.counter("pab.rpg.actions.resolved", "actionType", actionType.name()).increment();

        CheckResolution resolution = checkResolver.resolve(
                attributeScore(session.getCharacter().getAttributes(), actionType.getAttribute()),
                0,
                0,
                actionType.getDifficulty()
        );

        session.advanceWorldTime(ACTION_DURATION);
        session = gameSessionRepository.saveAndFlush(session);

        UUID actionId = UUID.randomUUID();
        int modifier = resolution.attributeModifier() + resolution.skillBonus() + resolution.circumstanceModifier();

        gameEventService.append(
                session.getId(),
                "ACTION_RESOLVED",
                command.playerId(),
                eventPayload(actionId, command, actionType, resolution, modifier),
                session.getWorldTime()
        );

        List<String> events = new ArrayList<>(List.of("ACTION_RESOLVED"));
        if (actionType == ActionType.SOCIAL && targetNpcId != null) {
            int delta = relationshipDeltaFor(resolution.grade());
            int newValue = npcService.changeRelationship(session.getId(), targetNpcId, delta);
            gameEventService.append(
                    session.getId(),
                    "RELATIONSHIP_CHANGED",
                    command.playerId(),
                    Map.of("npcId", targetNpcId, "delta", delta, "newValue", newValue),
                    session.getWorldTime()
            );
            events.add("RELATIONSHIP_CHANGED");
        }

        ActionResponse response = new ActionResponse(
                actionId,
                "RESOLVED",
                narrate(session, command.text(), resolution.grade(), String.join(", ", events), recentConversation),
                new ActionResponse.ResultDetails(
                        resolution.grade().name(),
                        new ActionResponse.RollDetails(resolution.d20(), modifier, resolution.total(), resolution.difficulty().getValue())
                ),
                events,
                session.getVersion()
        );

        idempotencyService.record(command.sessionId(), command.idempotencyKey(), actionId, response);

        return response;
    }

    private void validate(SubmitActionCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.sessionId(), "sessionId must not be null");
        Objects.requireNonNull(command.playerId(), "playerId must not be null");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey must not be null");

        if (command.text() == null || command.text().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (command.text().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("text must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
    }

    private int attributeScore(AttributeSet attributes, Attribute attribute) {
        return switch (attribute) {
            case STRENGTH -> attributes.getStrength();
            case AGILITY -> attributes.getAgility();
            case INTELLECT -> attributes.getIntellect();
            case WILLPOWER -> attributes.getWillpower();
            case PERCEPTION -> attributes.getPerception();
            case PRESENCE -> attributes.getPresence();
        };
    }

    private Map<String, Object> eventPayload(UUID actionId, SubmitActionCommand command, ActionType actionType, CheckResolution resolution, int modifier) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("actionId", actionId);
        payload.put("text", command.text());
        payload.put("actionType", actionType.name());
        payload.put("attribute", actionType.getAttribute().name());
        payload.put("seed", resolution.seed());
        payload.put("d20", resolution.d20());
        payload.put("modifier", modifier);
        payload.put("total", resolution.total());
        payload.put("difficulty", resolution.difficulty().getValue());
        payload.put("margin", resolution.margin());
        payload.put("grade", resolution.grade().name());
        return payload;
    }

    private int relationshipDeltaFor(ResultGrade grade) {
        return switch (grade) {
            case GRAN_EXITO -> 2;
            case EXITO -> 1;
            case EXITO_CON_COSTE -> 0;
            case FRACASO -> -1;
            case FRACASO_GRAVE -> -2;
        };
    }

    // Reachable before gameRules runs (interpret() needs the scene summary), so this must reject with the
    // same domain error as LocationExistsRule instead of assuming the location was already validated.
    private String sceneSummary(UUID locationId) {
        Location location = Optional.ofNullable(locationId)
                .flatMap(locationRepository::findById)
                .orElseThrow(() -> new ActionNotAllowedException("La sesión no está en una localización válida."));
        return location.getName() + " — " + location.getDescription();
    }

    private List<VisibleNpc> visibleNpcs(UUID locationId) {
        return npcService.getNpcsAtLocation(locationId).stream()
                .map(npc -> new VisibleNpc(npc.getId(), npc.getName()))
                .toList();
    }

    // Narrates and records the turn in the same step so no caller can narrate without it entering memory.
    private String narrate(GameSession session, String actionText, ResultGrade grade, String eventsSummary, String recentConversation) {
        String narration = masterAdapter.narrate(new NarrationRequest(
                sceneSummary(session.getCurrentLocationId()), actionText, grade, eventsSummary, recentConversation
        ));
        conversationMemoryService.recordTurn(session.getId(), actionText, narration);
        return narration;
    }
}
