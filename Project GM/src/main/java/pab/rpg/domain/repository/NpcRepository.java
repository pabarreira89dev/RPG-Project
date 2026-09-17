package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.entity.Npc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NpcRepository extends JpaRepository<Npc, UUID> {

    Optional<Npc> findByCode(String code);

    List<Npc> findAllByLocationId(UUID locationId);
}
