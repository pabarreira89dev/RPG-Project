package pab.rpg.domain.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pab.rpg.domain.entity.Npc;
import pab.rpg.domain.repository.NpcRepository;
import pab.rpg.exception.ActionNotAllowedException;

import java.util.UUID;

// When an action targets an NPC, that NPC must exist and be in the actor's current location.
@Component
@RequiredArgsConstructor
public class NpcTargetRule implements GameRule {

    private final NpcRepository npcRepository;

    @Override
    public void check(ActionContext context) {
        UUID npcId = context.targetNpcId();
        if (npcId == null) {
            return;
        }

        Npc npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new ActionNotAllowedException("El NPC indicado no existe."));

        if (!npc.getLocationId().equals(context.session().getCurrentLocationId())) {
            throw new ActionNotAllowedException("El NPC indicado no está en la localización actual.");
        }
    }
}
