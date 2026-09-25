package pab.rpg.auth.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.auth.exception.UsernameAlreadyExistsException;
import pab.rpg.auth.service.AppUserService;

// JSON registration API for the Android app (see PasswordGrantAuthenticationProvider for login) —
// the only way to create an account; this service has no browser-facing forms of any kind.
@RestController
@RequiredArgsConstructor
public class AuthApiController {

    private final AppUserService appUserService;

    @PostMapping("/api/v1/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        appUserService.register(request.username(), request.password(), request.email());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUsernameAlreadyExists(UsernameAlreadyExistsException exception) {
        return new ApiError("USERNAME_ALREADY_EXISTS", exception.getMessage());
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8, max = 100) String password,
            @Email String email
    ) {
    }

    public record ApiError(String code, String message) {
    }
}
