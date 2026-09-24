package pab.rpg.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.memory.ConversationTurn;
import pab.rpg.domain.repository.ConversationTurnRepository;
import pab.rpg.service.ConversationMemoryService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private final ConversationTurnRepository conversationTurnRepository;

    @Override
    public void recordTurn(UUID sessionId, String playerText, String narration) {
        long nextSequence = conversationTurnRepository.findTopBySessionIdOrderBySequenceDesc(sessionId)
                .map(turn -> turn.getSequence() + 1)
                .orElse(1L);

        conversationTurnRepository.save(new ConversationTurn(null, sessionId, nextSequence, playerText, narration, Instant.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public String summarizeRecent(UUID sessionId) {
        List<ConversationTurn> recentDescending = conversationTurnRepository.findTop5BySessionIdOrderBySequenceDesc(sessionId);
        if (recentDescending.isEmpty()) {
            return "";
        }

        List<ConversationTurn> chronological = new ArrayList<>(recentDescending);
        Collections.reverse(chronological);

        return chronological.stream()
                .map(turn -> "Jugador: " + turn.getPlayerText() + "\nMaster: " + turn.getNarration())
                .collect(Collectors.joining("\n"));
    }
}
