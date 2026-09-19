package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.response.GameEventResponse;
import pab.rpg.service.GameEventService;
import pab.rpg.service.GameSessionService;

import java.util.List;
import java.util.UUID;

// Diagnostic-only endpoint; the normal client does not consume raw internal events.
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/events")
@RequiredArgsConstructor
public class GameEventController {

    private final GameSessionService gameSessionService;
    private final GameEventService gameEventService;

    @GetMapping
    public List<GameEventResponse> getEvents(
            @PathVariable UUID sessionId,
            @RequestParam UUID playerId,
            @RequestParam(defaultValue = "0") long after
    ) {
        gameSessionService.getSession(sessionId, playerId);

        return gameEventService.findAfter(sessionId, after).stream()
                .map(GameEventResponse::from)
                .toList();
    }
}
