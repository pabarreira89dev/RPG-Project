package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.entity.Location;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByCode(String code);
}
