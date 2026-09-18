package pab.rpg.service;

import pab.rpg.domain.rules.ActionType;
import pab.rpg.domain.rules.ResultGrade;

import java.util.List;
import java.util.UUID;

// Isolates the domain from the AI provider (ARCHITECTURE_CONTRACT.md): OpenAI only narrates an
// already-resolved result, it never decides the result itself, and it only proposes an actionType/target
// that the Game Engine still has to validate (NpcTargetRule) before it's ever trusted.
public interface MasterAdapter {

    String narrate(NarrationRequest request);

    ActionIntent interpret(InterpretationRequest request);

    // Generic "pick one of these" resolution reused by combat (attack target) and quests (choiceKey):
    // returns the id of the chosen Candidate, or null if none matches the player's text. Never invents
    // an id outside the given candidates; callers must still validate the returned id against real state
    // (e.g. NpcTargetRule for NPCs, findParticipant for combat, the transition lookup for quests).
    String selectCandidate(CandidateSelectionRequest request);

    record NarrationRequest(String sceneSummary, String actionText, ResultGrade grade, String eventsSummary) {
    }

    record InterpretationRequest(String sceneSummary, String playerText, List<VisibleNpc> visibleNpcs) {
    }

    record VisibleNpc(UUID id, String name) {
    }

    // targetNpcId is null when no NPC from visibleNpcs was identified as the target; never an invented id.
    record ActionIntent(ActionType actionType, UUID targetNpcId) {
    }

    record CandidateSelectionRequest(String playerText, List<Candidate> candidates) {
    }

    record Candidate(String id, String label) {
    }
}

