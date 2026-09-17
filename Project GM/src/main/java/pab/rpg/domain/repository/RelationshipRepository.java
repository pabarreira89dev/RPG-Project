package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.entity.Relationship;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelationshipRepository extends JpaRepository<Relationship, UUID> {

    Optional<Relationship> findBySessionIdAndNpcId(UUID sessionId, UUID npcId);

    List<Relationship> findAllBySessionId(UUID sessionId);
}
