package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.item.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByIdAndSessionId(UUID id, UUID sessionId);

    List<Item> findAllBySessionIdAndOwnerId(UUID sessionId, UUID ownerId);

    List<Item> findAllBySessionIdAndLocationId(UUID sessionId, UUID locationId);
}
