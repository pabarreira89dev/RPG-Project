package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.quest.QuestState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestStateRepository extends JpaRepository<QuestState, UUID> {

    Optional<QuestState> findBySessionIdAndQuestId(UUID sessionId, UUID questId);

    List<QuestState> findAllBySessionId(UUID sessionId);
}
