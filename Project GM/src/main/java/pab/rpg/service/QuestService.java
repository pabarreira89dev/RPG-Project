package pab.rpg.service;

import pab.rpg.domain.quest.QuestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestService {

    // Idempotent: returns the existing state if the quest was already started for this session.
    QuestStateView startQuest(UUID sessionId, String questCode);

    QuestStateView advanceQuest(UUID sessionId, String questCode, String choiceKey);

    // Derives choiceKey from free text, resolved against the current stage's available transitions
    // via MasterAdapter.selectCandidate.
    QuestStateView advanceQuestFromText(UUID sessionId, String questCode, String playerText);

    Optional<QuestStateView> getQuestState(UUID sessionId, String questCode);

    List<QuestStateView> getVisibleQuests(UUID sessionId);

    record QuestStateView(
            String questCode,
            String questTitle,
            String stageCode,
            String stageDescription,
            QuestStatus status
    ) {
    }
}
