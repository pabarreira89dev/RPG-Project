package pab.rpg.service.impl;

import org.junit.jupiter.api.Test;
import pab.rpg.domain.rules.ActionType;
import pab.rpg.domain.rules.ResultGrade;
import pab.rpg.service.MasterAdapter.ActionIntent;
import pab.rpg.service.MasterAdapter.Candidate;
import pab.rpg.service.MasterAdapter.CandidateSelectionRequest;
import pab.rpg.service.MasterAdapter.InterpretationRequest;
import pab.rpg.service.MasterAdapter.NarrationRequest;
import pab.rpg.service.MasterAdapter.VisibleNpc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StubMasterAdapterTest {

    private final StubMasterAdapter adapter = new StubMasterAdapter();

    @Test
    void narratesEachResultGradeInSpanish() {
        assertEquals("Tu intento tiene un éxito rotundo.",
                adapter.narrate(new NarrationRequest("", "", ResultGrade.GRAN_EXITO, "")));
        assertEquals("Tu intento sale bien.",
                adapter.narrate(new NarrationRequest("", "", ResultGrade.EXITO, "")));
        assertEquals("Lo consigues, pero a cierto coste.",
                adapter.narrate(new NarrationRequest("", "", ResultGrade.EXITO_CON_COSTE, "")));
        assertEquals("Tu intento no sale como esperabas.",
                adapter.narrate(new NarrationRequest("", "", ResultGrade.FRACASO, "")));
        assertEquals("Tu intento fracasa de forma grave.",
                adapter.narrate(new NarrationRequest("", "", ResultGrade.FRACASO_GRAVE, "")));
    }

    @Test
    void interpretsActionTypeFromKeywords() {
        assertEquals(ActionType.PHYSICAL,
                adapter.interpret(new InterpretationRequest("", "Ataco al guardia", List.of())).actionType());
        assertEquals(ActionType.SOCIAL,
                adapter.interpret(new InterpretationRequest("", "Hablo con el tabernero", List.of())).actionType());
        assertEquals(ActionType.INVESTIGATION,
                adapter.interpret(new InterpretationRequest("", "Busco pistas en la sala", List.of())).actionType());
        assertEquals(ActionType.EXPLORATION,
                adapter.interpret(new InterpretationRequest("", "Miro alrededor", List.of())).actionType());
    }

    @Test
    void interpretsTargetNpcWhenNameIsMentioned() {
        UUID npcId = UUID.randomUUID();
        List<VisibleNpc> visibleNpcs = List.of(new VisibleNpc(npcId, "Aron"));

        ActionIntent intent = adapter.interpret(new InterpretationRequest("", "Hablo con Aron", visibleNpcs));

        assertEquals(npcId, intent.targetNpcId());
    }

    @Test
    void doesNotInventATargetWhenNoNpcIsMentioned() {
        List<VisibleNpc> visibleNpcs = List.of(new VisibleNpc(UUID.randomUUID(), "Aron"));

        ActionIntent intent = adapter.interpret(new InterpretationRequest("", "Miro alrededor", visibleNpcs));

        assertNull(intent.targetNpcId());
    }

    @Test
    void selectsCandidateWhenLabelIsMentioned() {
        Candidate hunter = new Candidate("1", "Cazador");
        Candidate merchant = new Candidate("2", "Mercader");

        String selected = adapter.selectCandidate(new CandidateSelectionRequest("Ataco al cazador", List.of(hunter, merchant)));

        assertEquals("1", selected);
    }

    @Test
    void doesNotInventACandidateWhenNoneIsMentioned() {
        Candidate hunter = new Candidate("1", "Cazador");

        String selected = adapter.selectCandidate(new CandidateSelectionRequest("Miro alrededor", List.of(hunter)));

        assertNull(selected);
    }
}
