package pab.rpg.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.memory.ConversationTurn;
import pab.rpg.domain.repository.ConversationTurnRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationMemoryServiceImplTest {

    @Mock
    private ConversationTurnRepository conversationTurnRepository;

    @Test
    void recordTurnStartsSequenceAtOneWhenNoneExists() {
        ConversationMemoryServiceImpl service = new ConversationMemoryServiceImpl(conversationTurnRepository);
        UUID sessionId = UUID.randomUUID();
        when(conversationTurnRepository.findTopBySessionIdOrderBySequenceDesc(sessionId)).thenReturn(Optional.empty());

        service.recordTurn(sessionId, "Miro alrededor", "No ves nada fuera de lo común.");

        verify(conversationTurnRepository).save(argThat(turn -> turn.getSequence() == 1L
                && turn.getPlayerText().equals("Miro alrededor")
                && turn.getNarration().equals("No ves nada fuera de lo común.")));
    }

    @Test
    void recordTurnIncrementsSequenceFromLastTurn() {
        ConversationMemoryServiceImpl service = new ConversationMemoryServiceImpl(conversationTurnRepository);
        UUID sessionId = UUID.randomUUID();
        ConversationTurn lastTurn = new ConversationTurn(UUID.randomUUID(), sessionId, 3L, "texto", "narracion", Instant.now());
        when(conversationTurnRepository.findTopBySessionIdOrderBySequenceDesc(sessionId)).thenReturn(Optional.of(lastTurn));

        service.recordTurn(sessionId, "Ataco al guardia", "El golpe acierta.");

        verify(conversationTurnRepository).save(argThat(turn -> turn.getSequence() == 4L));
    }

    @Test
    void summarizeRecentReturnsEmptyStringWhenNoHistory() {
        ConversationMemoryServiceImpl service = new ConversationMemoryServiceImpl(conversationTurnRepository);
        UUID sessionId = UUID.randomUUID();
        when(conversationTurnRepository.findTop5BySessionIdOrderBySequenceDesc(sessionId)).thenReturn(List.of());

        assertEquals("", service.summarizeRecent(sessionId));
    }

    @Test
    void summarizeRecentOrdersTurnsChronologically() {
        ConversationMemoryServiceImpl service = new ConversationMemoryServiceImpl(conversationTurnRepository);
        UUID sessionId = UUID.randomUUID();
        ConversationTurn newer = new ConversationTurn(UUID.randomUUID(), sessionId, 2L, "Ataco", "El golpe acierta.", Instant.now());
        ConversationTurn older = new ConversationTurn(UUID.randomUUID(), sessionId, 1L, "Miro alrededor", "No ves nada.", Instant.now());
        // Repository query is ORDER BY sequence DESC, so the newest turn comes first here.
        when(conversationTurnRepository.findTop5BySessionIdOrderBySequenceDesc(sessionId)).thenReturn(List.of(newer, older));

        String summary = service.summarizeRecent(sessionId);

        assertTrue(summary.indexOf("Miro alrededor") < summary.indexOf("Ataco"));
        assertEquals("Jugador: Miro alrededor\nMaster: No ves nada.\nJugador: Ataco\nMaster: El golpe acierta.", summary);
    }
}
