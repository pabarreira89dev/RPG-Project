package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.entity.QuestStage;

import java.util.Optional;
import java.util.UUID;

public interface QuestStageRepository extends JpaRepository<QuestStage, UUID> {

    Optional<QuestStage> findByQuestIdAndCode(UUID questId, String code);

    Optional<QuestStage> findByQuestIdAndInitialTrue(UUID questId);
}
