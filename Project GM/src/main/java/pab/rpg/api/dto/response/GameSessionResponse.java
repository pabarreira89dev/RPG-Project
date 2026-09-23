package pab.rpg.api.dto.response;

import lombok.Builder;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.session.SessionStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
public record GameSessionResponse(
        UUID sessionId,
        UUID playerId,
        UUID worldId,
        UUID currentLocationId,
        SessionStatus status,
        Instant worldTime,
        long version,
        CharacterResponse character
) {

    public static GameSessionResponse from(GameSession session) {
        return GameSessionResponse.builder()
                .sessionId(session.getId())
                .playerId(session.getPlayerId())
                .worldId(session.getWorldId())
                .currentLocationId(session.getCurrentLocationId())
                .status(session.getStatus())
                .worldTime(session.getWorldTime())
                .version(session.getVersion())
                .character(CharacterResponse.from(session.getCharacter()))
                .build();
    }

    @Builder
    public record CharacterResponse(
            UUID id,
            String name,
            int level,
            int experience,
            AttributeSet attributes,
            HealthState health
    ) {

        private static CharacterResponse from(Character character) {
            return CharacterResponse.builder()
                    .id(character.getId())
                    .name(character.getName())
                    .level(character.getLevel())
                    .experience(character.getExperience())
                    .attributes(character.getAttributes())
                    .health(character.getHealth())
                    .build();
        }
    }
}
