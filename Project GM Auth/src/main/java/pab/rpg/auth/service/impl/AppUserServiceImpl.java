package pab.rpg.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.auth.domain.AppUser;
import pab.rpg.auth.domain.repository.AppUserRepository;
import pab.rpg.auth.exception.UsernameAlreadyExistsException;
import pab.rpg.auth.service.AppUserService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppUser register(String username, String rawPassword, String email) {
        if (appUserRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Ya existe un usuario con ese nombre.");
        }

        AppUser appUser = new AppUser(
                null,
                username,
                passwordEncoder.encode(rawPassword),
                email,
                Instant.now(),
                true
        );
        return appUserRepository.save(appUser);
    }
}
