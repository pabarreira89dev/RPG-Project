package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.request.AdvanceQuestRequest;
import pab.rpg.api.dto.response.QuestStateResponse;
import pab.rpg.security.CurrentPlayer;
import pab.rpg.service.GameSessionService;
import pab.rpg.service.QuestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/quests")
@RequiredArgsConstructor
public class QuestController {

    private final GameSessionService gameSessionService;
    private final QuestService questService;

    @GetMapping
    public List<QuestStateResponse> getVisibleQuests(
            @PathVariable UUID sessionId,
            @CurrentPlayer UUID playerId
    ) {
        gameSessionService.getSession(sessionId, playerId);

        return questService.getVisibleQuests(sessionId).stream()
                .map(QuestStateResponse::from)
                .toList();
    }

    @PostMapping("/{questCode}/start")
    public QuestStateResponse startQuest(
            @PathVariable UUID sessionId,
            @PathVariable String questCode,
            @CurrentPlayer UUID playerId
    ) {
        gameSessionService.getSession(sessionId, playerId);

        return QuestStateResponse.from(questService.startQuest(sessionId, questCode));
    }

    @PostMapping("/{questCode}/advance")
    public QuestStateResponse advanceQuest(
            @PathVariable UUID sessionId,
            @PathVariable String questCode,
            @CurrentPlayer UUID playerId,
            @RequestBody AdvanceQuestRequest request
    ) {
        gameSessionService.getSession(sessionId, playerId);

        QuestService.QuestStateView view = request.choiceKey() != null
                ? questService.advanceQuest(sessionId, questCode, request.choiceKey())
                : questService.advanceQuestFromText(sessionId, questCode, request.text());

        return QuestStateResponse.from(view);
    }
}
