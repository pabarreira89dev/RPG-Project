package pab.rpg.domain.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.exception.ActionNotAllowedException;

// The session must be anchored to a real, seeded location before any action resolves.
@Component
@RequiredArgsConstructor
public class LocationExistsRule implements GameRule {

    private final LocationRepository locationRepository;

    @Override
    public void check(ActionContext context) {
        var locationId = context.session().getCurrentLocationId();
        if (locationId == null || !locationRepository.existsById(locationId)) {
            throw new ActionNotAllowedException("La sesión no está en una localización válida.");
        }
    }
}
