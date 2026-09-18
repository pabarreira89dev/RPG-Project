package pab.rpg.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pab.rpg.domain.rules.ActionType;
import pab.rpg.service.MasterAdapter;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

// Deterministic fallback used while openai.enabled=false (local/test profiles); same wording as the
// provisional ActionServiceImpl narration, kept here so it can be swapped in without behavior changes.
@Service
@ConditionalOnProperty(prefix = "openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class StubMasterAdapter implements MasterAdapter {

    @Override
    public String narrate(NarrationRequest request) {
        return switch (request.grade()) {
            case GRAN_EXITO -> "Tu intento tiene un éxito rotundo.";
            case EXITO -> "Tu intento sale bien.";
            case EXITO_CON_COSTE -> "Lo consigues, pero a cierto coste.";
            case FRACASO -> "Tu intento no sale como esperabas.";
            case FRACASO_GRAVE -> "Tu intento fracasa de forma grave.";
        };
    }

    // Simple keyword heuristic, no network calls; OpenAiMasterAdapter does the real interpretation.
    @Override
    public ActionIntent interpret(InterpretationRequest request) {
        String text = request.playerText().toLowerCase(Locale.ROOT);
        return new ActionIntent(actionTypeFor(text), targetNpcFor(text, request.visibleNpcs()));
    }

    // Picks the first candidate whose label is mentioned in the text; null if none match.
    @Override
    public String selectCandidate(CandidateSelectionRequest request) {
        String text = request.playerText().toLowerCase(Locale.ROOT);
        return request.candidates().stream()
                .filter(candidate -> text.contains(candidate.label().toLowerCase(Locale.ROOT)))
                .map(Candidate::id)
                .findFirst()
                .orElse(null);
    }

    private ActionType actionTypeFor(String text) {
        if (containsAny(text, "atac", "golpe", "pelea", "lanz", "empuj")) {
            return ActionType.PHYSICAL;
        }
        if (containsAny(text, "habl", "convenc", "pregunt", "negoci", "salud")) {
            return ActionType.SOCIAL;
        }
        if (containsAny(text, "busc", "investig", "examin", "inspeccion")) {
            return ActionType.INVESTIGATION;
        }
        return ActionType.EXPLORATION;
    }

    private UUID targetNpcFor(String text, List<VisibleNpc> visibleNpcs) {
        return visibleNpcs.stream()
                .filter(npc -> text.contains(npc.name().toLowerCase(Locale.ROOT)))
                .map(VisibleNpc::id)
                .findFirst()
                .orElse(null);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
