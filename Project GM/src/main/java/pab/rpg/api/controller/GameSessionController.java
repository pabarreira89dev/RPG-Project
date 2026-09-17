package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.request.CreateSessionRequest;
import pab.rpg.api.mapper.CreateSessionRequestMapper;
import pab.rpg.api.dto.response.GameSessionResponse;
import pab.rpg.domain.entity.GameSession;
import pab.rpg.service.GameSerssionService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class GameSessionController {

    private final GameSerssionService gameSessionService;
    private final CreateSessionRequestMapper createSessionRequestMapper;

    @PostMapping
    public ResponseEntity<GameSessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        GameSession session = gameSessionService.createSession(createSessionRequestMapper.toCommand(request));
        GameSessionResponse response = GameSessionResponse.from(session);

        return ResponseEntity
                .created(URI.create("/api/v1/sessions/" + session.getId()))
                .body(response);
    }

    @GetMapping("/{sessionId}")
    public GameSessionResponse getSession(
            @PathVariable UUID sessionId,
            @RequestParam UUID playerId
    ) {
        return GameSessionResponse.from(gameSessionService.getSession(sessionId, playerId));
    }

    @GetMapping
    public List<GameSessionResponse> getSessions(@RequestParam UUID playerId) {
        return gameSessionService.getSessions(playerId).stream()
                .map(GameSessionResponse::from)
                .toList();
    }
}
