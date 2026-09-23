package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.response.NpcResponse;
import pab.rpg.domain.session.GameSession;
import pab.rpg.security.CurrentPlayer;
import pab.rpg.service.GameSessionService;
import pab.rpg.service.NpcService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/npcs")
@RequiredArgsConstructor
public class NpcController {

    private final GameSessionService gameSessionService;
    private final NpcService npcService;

    @GetMapping
    public List<NpcResponse> getNpcsAtCurrentLocation(
            @PathVariable UUID sessionId,
            @CurrentPlayer UUID playerId
    ) {
        GameSession session = gameSessionService.getSession(sessionId, playerId);

        return npcService.getNpcsAtLocation(session.getCurrentLocationId()).stream()
                .map(npc -> NpcResponse.from(npc, npcService.getRelationshipValue(sessionId, npc.getId())))
                .toList();
    }
}
