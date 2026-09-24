package pab.rpg.service;

import java.util.UUID;

public interface ConversationMemoryService {

    void recordTurn(UUID sessionId, String playerText, String narration);

    // Chronological "Jugador: ...\nMaster: ..." summary of the last few turns; "" when the session has none yet.
    String summarizeRecent(UUID sessionId);
}
