package pab.rpg.api.dto.response;

import lombok.Builder;
import pab.rpg.domain.entity.GameEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
public record GameEventResponse(
        UUID eventId,
        UUID sessionId,
        long sequence,
        String type,
        UUID actorId,
        Map<String, Object> payload,
        Instant worldTime,
        Instant createdAt
) {

    public static GameEventResponse from(GameEvent event) {
        return GameEventResponse.builder()
                .eventId(event.getId())
                .sessionId(event.getSessionId())
                .sequence(event.getSequence())
                .type(event.getType())
                .actorId(event.getActorId())
                .payload(event.getPayload())
                .worldTime(event.getWorldTime())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
