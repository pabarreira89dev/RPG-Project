package pab.rpg.domain.rules;

import org.springframework.stereotype.Component;
import pab.rpg.exception.ActionNotAllowedException;

// The character must be alive to attempt any action.
@Component
public class ActorAliveRule implements GameRule {

    @Override
    public void check(ActionContext context) {
        if (context.session().getCharacter().getHealth().getCurrent() <= 0) {
            throw new ActionNotAllowedException("El personaje no puede actuar: no tiene salud.");
        }
    }
}
