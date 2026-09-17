package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.entity.QuestStageTransition;

import java.util.Optional;
import java.util.UUID;

public interface QuestStageTransitionRepository extends JpaRepository<QuestStageTransition, UUID> {

    Optional<QuestStageTransition> findByQuestIdAndFromStageIdAndChoiceKey(UUID questId, UUID fromStageId, String choiceKey);
}
