package pab.rpg.domain.repository;

import pab.rpg.domain.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CharacterRepository extends JpaRepository<Character, UUID> {
}
