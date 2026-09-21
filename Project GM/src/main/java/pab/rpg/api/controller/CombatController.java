package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.request.PerformAttackRequest;
import pab.rpg.api.dto.request.StartCombatRequest;
import pab.rpg.api.dto.response.CombatResponse;
import pab.rpg.exception.CombatNotFoundException;
import pab.rpg.security.CurrentPlayer;
import pab.rpg.service.CombatService;
import pab.rpg.service.GameSessionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/combat")
@RequiredArgsConstructor
public class CombatController {

    private final GameSessionService gameSessionService;
    private final CombatService combatService;

    @GetMapping
    public CombatResponse getActiveCombat(
            @PathVariable UUID sessionId,
            @CurrentPlayer UUID playerId
    ) {
        gameSessionService.getSession(sessionId, playerId);

        return combatService.getActiveCombat(sessionId)
                .map(CombatResponse::from)
                .orElseThrow(() -> new CombatNotFoundException("No hay ningún combate activo en esta partida."));
    }

    @PostMapping("/start")
    public CombatResponse startCombat(
            @PathVariable UUID sessionId,
            @CurrentPlayer UUID playerId,
            @RequestBody StartCombatRequest request
    ) {
        gameSessionService.getSession(sessionId, playerId);

        return CombatResponse.from(combatService.startCombat(sessionId, request.npcIds()));
    }

    @PostMapping("/{combatId}/attack")
    public CombatResponse performAttack(
            @PathVariable UUID sessionId,
            @PathVariable UUID combatId,
            @CurrentPlayer UUID playerId,
            @RequestBody PerformAttackRequest request
    ) {
        gameSessionService.getSession(sessionId, playerId);

        CombatService.CombatView view = request.targetParticipantId() != null
                ? combatService.performAttack(sessionId, combatId, request.attackerParticipantId(), request.targetParticipantId())
                : combatService.performAttack(sessionId, combatId, request.text());

        return CombatResponse.from(view);
    }
}
