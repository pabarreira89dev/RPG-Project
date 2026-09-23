package pab.rpg.api.dto.response;

import lombok.Builder;
import pab.rpg.domain.quest.QuestStatus;
import pab.rpg.service.QuestService;

@Builder
public record QuestStateResponse(
        String questCode,
        String questTitle,
        String stageCode,
        String stageDescription,
        QuestStatus status
) {

    public static QuestStateResponse from(QuestService.QuestStateView view) {
        return QuestStateResponse.builder()
                .questCode(view.questCode())
                .questTitle(view.questTitle())
                .stageCode(view.stageCode())
                .stageDescription(view.stageDescription())
                .status(view.status())
                .build();
    }
}
