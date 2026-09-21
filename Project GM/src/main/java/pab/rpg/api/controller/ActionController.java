package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.SubmitActionCommand;
import pab.rpg.api.dto.request.SubmitActionRequest;
import pab.rpg.api.dto.response.ActionResponse;
import pab.rpg.security.CurrentPlayer;
import pab.rpg.service.ActionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @PostMapping
    public ActionResponse submitAction(
            @PathVariable UUID sessionId,
            @CurrentPlayer UUID playerId,
            @RequestBody SubmitActionRequest request
    ) {
        SubmitActionCommand command = new SubmitActionCommand(
                sessionId,
                playerId,
                request.text(),
                request.actionType(),
                request.targetNpcId(),
                request.expectedVersion(),
                request.idempotencyKey()
        );

        return actionService.submitAction(command);
    }
}
