package pab.rpg.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import pab.rpg.auth.domain.repository.AppUserRepository;

// Everything that isn't an Authorization Server endpoint: just the JSON registration API the Android
// app uses. No browser-facing pages/forms of any kind — this service only ever receives/returns JSON.
@Configuration
@RequiredArgsConstructor
public class DefaultSecurityConfig {

    private final AppUserRepository appUserRepository;

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is for cookie/session-authenticated browser forms, which don't exist here;
                // the JSON API is called by the native app with no session/cookie, so it doesn't apply.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/register"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/register").permitAll()
                        .anyRequest().authenticated())
                // No formLogin/login page of any kind — an unauthenticated request to anything else just
                // gets a plain 401, never HTML.
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> appUserRepository.findByUsername(username)
                .map(appUser -> User.withUsername(appUser.getUsername())
                        .password(appUser.getPasswordHash())
                        .disabled(!appUser.isEnabled())
                        .authorities("ROLE_USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
