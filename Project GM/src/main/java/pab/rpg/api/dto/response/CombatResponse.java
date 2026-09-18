package pab.rpg.api.dto.response;

import lombok.Builder;
import pab.rpg.domain.entity.CombatParticipantStatus;
import pab.rpg.domain.entity.CombatStatus;
import pab.rpg.domain.entity.CombatTeam;
import pab.rpg.service.CombatService;

import java.util.List;
import java.util.UUID;

@Builder
public record CombatResponse(
        UUID combatId,
        CombatStatus status,
        int roundNumber,
        UUID currentParticipantId,
        List<ParticipantResponse> participants,
        String narration
) {

    public static CombatResponse from(CombatService.CombatView view) {
        return CombatResponse.builder()
                .combatId(view.combatId())
                .status(view.status())
                .roundNumber(view.roundNumber())
                .currentParticipantId(view.currentParticipantId())
                .participants(view.participants().stream().map(ParticipantResponse::from).toList())
                .narration(view.narration())
                .build();
    }

    @Builder
    public record ParticipantResponse(
            UUID id,
            String name,
            CombatTeam team,
            int initiative,
            int turnOrder,
            int actionsRemaining,
            int healthCurrent,
            int healthMaximum,
            CombatParticipantStatus status
    ) {

        public static ParticipantResponse from(CombatService.ParticipantView view) {
            return ParticipantResponse.builder()
                    .id(view.id())
                    .name(view.name())
                    .team(view.team())
                    .initiative(view.initiative())
                    .turnOrder(view.turnOrder())
                    .actionsRemaining(view.actionsRemaining())
                    .healthCurrent(view.healthCurrent())
                    .healthMaximum(view.healthMaximum())
                    .status(view.status())
                    .build();
        }
    }
}
