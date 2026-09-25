package pab.rpg.auth.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pab.rpg.auth.domain.AppUser;
import pab.rpg.auth.domain.repository.AppUserRepository;
import pab.rpg.auth.exception.UsernameAlreadyExistsException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void register_hashesPasswordAndPersistsNewUser() {
        AppUserServiceImpl service = new AppUserServiceImpl(appUserRepository, passwordEncoder);

        when(appUserRepository.existsByUsername("aron")).thenReturn(false);
        when(passwordEncoder.encode("s3cret!!")).thenReturn("hashed");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = service.register("aron", "s3cret!!", "aron@example.com");

        assertThat(result.getUsername()).isEqualTo("aron");
        assertThat(result.getPasswordHash()).isEqualTo("hashed");
        assertThat(result.getEmail()).isEqualTo("aron@example.com");
        assertThat(result.isEnabled()).isTrue();
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void register_rejectsDuplicateUsername() {
        AppUserServiceImpl service = new AppUserServiceImpl(appUserRepository, passwordEncoder);

        when(appUserRepository.existsByUsername("aron")).thenReturn(true);

        assertThatThrownBy(() -> service.register("aron", "s3cret!!", null))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }
}
