package pab.rpg.auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.auth.domain.AppUser;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
