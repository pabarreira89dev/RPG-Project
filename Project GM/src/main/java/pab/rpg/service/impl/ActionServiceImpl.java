package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.api.dto.SubmitActionCommand;
import pab.rpg.api.dto.response.ActionResponse;
import pab.rpg.domain.entity.AttributeSet;
import pab.rpg.domain.entity.GameSession;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.rules.ActionContext;
import pab.rpg.domain.rules.ActionType;
import pab.rpg.domain.rules.Attribute;
import pab.rpg.domain.rules.CheckResolution;
import pab.rpg.domain.rules.CheckResolver;
import pab.rpg.domain.rules.GameRule;
import pab.rpg.domain.rules.ResultGrade;
import pab.rpg.exception.StaleSessionVersionException;
import pab.rpg.service.ActionService;
import pab.rpg.service.GameEventService;
import pab.rpg.service.GameSerssionService;
import pab.rpg.service.IdempotencyService;
import pab.rpg.service.NpcService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ActionServiceImpl implements ActionService {

    private static final Duration ACTION_DURATION = Duration.ofMinutes(10);
    private static final int MAX_TEXT_LENGTH = 2000;

    private final GameSerssionService gameSessionService;
    private final GameSessionRepository gameSessionRepository;
    private final GameEventService gameEventService;
    private final IdempotencyService idempotencyService;
    private final CheckResolver checkResolver;
    private final List<GameRule> gameRules;
    private final NpcService npcService;
    private final ObjectMapper objectMapper;

    @Override
    public ActionResponse submitAction(SubmitActionCommand command) {
        validate(command);

        GameSession session = gameSessionService.getSession(command.sessionId(), command.playerId());

        Optional<IdempotencyService.StoredActionResult> existing =
                idempotencyService.findExisting(command.sessionId(), command.idempotencyKey());
        if (existing.isPresent()) {
            return objectMapper.convertValue(existing.get().responsePayload(), ActionResponse.class);
        }

        if (session.getVersion() != command.expectedVersion()) {
            throw new StaleSessionVersionException(command.sessionId(), command.expectedVersion(), session.getVersion());
        }

        ActionContext context = new ActionContext(session, command.actionType(), command.targetNpcId());
        gameRules.forEach(rule -> rule.check(context));

        CheckResolution resolution = checkResolver.resolve(
                attributeScore(session.getCharacter().getAttributes(), command.actionType().getAttribute()),
                0,
                0,
                command.actionType().getDifficulty()
        );

        session.advanceWorldTime(ACTION_DURATION);
        session = gameSessionRepository.saveAndFlush(session);

        UUID actionId = UUID.randomUUID();
        int modifier = resolution.attributeModifier() + resolution.skillBonus() + resolution.circumstanceModifier();

        gameEventService.append(
                session.getId(),
                "ACTION_RESOLVED",
                command.playerId(),
                eventPayload(actionId, command, resolution, modifier),
                session.getWorldTime()
        );

        List<String> events = new ArrayList<>(List.of("ACTION_RESOLVED"));
        if (command.actionType() == ActionType.SOCIAL && command.targetNpcId() != null) {
            int delta = relationshipDeltaFor(resolution.grade());
            int newValue = npcService.changeRelationship(session.getId(), command.targetNpcId(), delta);
            gameEventService.append(
                    session.getId(),
                    "RELATIONSHIP_CHANGED",
                    command.playerId(),
                    Map.of("npcId", command.targetNpcId(), "delta", delta, "newValue", newValue),
                    session.getWorldTime()
            );
            events.add("RELATIONSHIP_CHANGED");
        }

        ActionResponse response = new ActionResponse(
                actionId,
                "RESOLVED",
                narrationFor(resolution),
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
        Objects.requireNonNull(command.actionType(), "actionType must not be null");
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

    private Map<String, Object> eventPayload(UUID actionId, SubmitActionCommand command, CheckResolution resolution, int modifier) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("actionId", actionId);
        payload.put("text", command.text());
        payload.put("actionType", command.actionType().name());
        payload.put("attribute", command.actionType().getAttribute().name());
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

    private String narrationFor(CheckResolution resolution) {
        return switch (resolution.grade()) {
            case GRAN_EXITO -> "Tu intento tiene un éxito rotundo.";
            case EXITO -> "Tu intento sale bien.";
            case EXITO_CON_COSTE -> "Lo consigues, pero a cierto coste.";
            case FRACASO -> "Tu intento no sale como esperabas.";
            case FRACASO_GRAVE -> "Tu intento fracasa de forma grave.";
        };
    }
}
