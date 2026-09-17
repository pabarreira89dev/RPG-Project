package pab.rpg.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.entity.Npc;
import pab.rpg.domain.entity.NpcKnowledgeFact;
import pab.rpg.domain.entity.Relationship;
import pab.rpg.domain.repository.NpcKnowledgeFactRepository;
import pab.rpg.domain.repository.NpcRepository;
import pab.rpg.domain.repository.RelationshipRepository;
import pab.rpg.service.NpcService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NpcServiceImpl implements NpcService {

    private final NpcRepository npcRepository;
    private final RelationshipRepository relationshipRepository;
    private final NpcKnowledgeFactRepository npcKnowledgeFactRepository;

    @Override
    public List<Npc> getNpcsAtLocation(UUID locationId) {
        return npcRepository.findAllByLocationId(locationId);
    }

    @Override
    public int getRelationshipValue(UUID sessionId, UUID npcId) {
        return relationshipRepository.findBySessionIdAndNpcId(sessionId, npcId)
                .map(Relationship::getValue)
                .orElse(0);
    }

    @Override
    @Transactional
    public int changeRelationship(UUID sessionId, UUID npcId, int delta) {
        Instant now = Instant.now();
        Relationship relationship = relationshipRepository.findBySessionIdAndNpcId(sessionId, npcId)
                .orElseGet(() -> new Relationship(null, sessionId, npcId, 0, now));

        relationship.changeBy(delta, now);

        return relationshipRepository.save(relationship).getValue();
    }

    @Override
    @Transactional
    public boolean recordKnowledge(UUID sessionId, UUID npcId, String factKey) {
        if (npcKnowledgeFactRepository.existsBySessionIdAndNpcIdAndFactKey(sessionId, npcId, factKey)) {
            return false;
        }

        npcKnowledgeFactRepository.save(new NpcKnowledgeFact(null, sessionId, npcId, factKey, Instant.now()));
        return true;
    }

    @Override
    public boolean knowsFact(UUID sessionId, UUID npcId, String factKey) {
        return npcKnowledgeFactRepository.existsBySessionIdAndNpcIdAndFactKey(sessionId, npcId, factKey);
    }
}
