package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.quest.QuestStageTransition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestStageTransitionRepository extends JpaRepository<QuestStageTransition, UUID> {

    Optional<QuestStageTransition> findByQuestIdAndFromStageIdAndChoiceKey(UUID questId, UUID fromStageId, String choiceKey);

    List<QuestStageTransition> findAllByQuestIdAndFromStageId(UUID questId, UUID fromStageId);
}
