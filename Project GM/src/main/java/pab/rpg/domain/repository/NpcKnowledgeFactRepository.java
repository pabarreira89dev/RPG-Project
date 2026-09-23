package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.npc.NpcKnowledgeFact;

import java.util.List;
import java.util.UUID;

public interface NpcKnowledgeFactRepository extends JpaRepository<NpcKnowledgeFact, UUID> {

    boolean existsBySessionIdAndNpcIdAndFactKey(UUID sessionId, UUID npcId, String factKey);

    List<NpcKnowledgeFact> findAllBySessionIdAndNpcId(UUID sessionId, UUID npcId);
}
