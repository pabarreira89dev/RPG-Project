package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.item.ItemTemplate;

import java.util.Optional;
import java.util.UUID;

public interface ItemTemplateRepository extends JpaRepository<ItemTemplate, UUID> {

    Optional<ItemTemplate> findByCode(String code);
}
