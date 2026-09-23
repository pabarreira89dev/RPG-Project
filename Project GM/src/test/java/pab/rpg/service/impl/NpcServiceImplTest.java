package pab.rpg.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.npc.Npc;
import pab.rpg.domain.npc.NpcKnowledgeFact;
import pab.rpg.domain.npc.NpcStatus;
import pab.rpg.domain.npc.Relationship;
import pab.rpg.domain.repository.NpcKnowledgeFactRepository;
import pab.rpg.domain.repository.NpcRepository;
import pab.rpg.domain.repository.RelationshipRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NpcServiceImplTest {

    @Mock
    private NpcRepository npcRepository;

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private NpcKnowledgeFactRepository npcKnowledgeFactRepository;

    private NpcServiceImpl service;

    @Test
    void getNpcsAtLocationReturnsRepositoryResult() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID locationId = UUID.randomUUID();
        Npc npc = new Npc(UUID.randomUUID(), "aron_tavernkeeper", "Aron", locationId, null, "Tabernero", NpcStatus.ALIVE,
                new AttributeSet(10, 10, 10, 10, 10, 10), 20);
        when(npcRepository.findAllByLocationId(locationId)).thenReturn(List.of(npc));

        List<Npc> result = service.getNpcsAtLocation(locationId);

        assertEquals(List.of(npc), result);
    }

    @Test
    void getRelationshipValueReturnsZeroWhenNoRelationshipExists() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID sessionId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        when(relationshipRepository.findBySessionIdAndNpcId(sessionId, npcId)).thenReturn(Optional.empty());

        assertEquals(0, service.getRelationshipValue(sessionId, npcId));
    }

    @Test
    void getRelationshipValueReturnsStoredValue() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID sessionId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        Relationship relationship = new Relationship(UUID.randomUUID(), sessionId, npcId, 3, Instant.now());
        when(relationshipRepository.findBySessionIdAndNpcId(sessionId, npcId)).thenReturn(Optional.of(relationship));

        assertEquals(3, service.getRelationshipValue(sessionId, npcId));
    }

    @Test
    void changeRelationshipCreatesRowStartingFromZeroWhenMissing() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID sessionId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        when(relationshipRepository.findBySessionIdAndNpcId(sessionId, npcId)).thenReturn(Optional.empty());
        when(relationshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        int result = service.changeRelationship(sessionId, npcId, 2);

        assertEquals(2, result);
    }

    @Test
    void changeRelationshipUpdatesExistingRow() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID sessionId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        Relationship relationship = new Relationship(UUID.randomUUID(), sessionId, npcId, 3, Instant.now());
        when(relationshipRepository.findBySessionIdAndNpcId(sessionId, npcId)).thenReturn(Optional.of(relationship));
        when(relationshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        int result = service.changeRelationship(sessionId, npcId, -1);

        assertEquals(2, result);
    }

    @Test
    void recordKnowledgeSavesFactWhenNotAlreadyKnown() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID sessionId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        when(npcKnowledgeFactRepository.existsBySessionIdAndNpcIdAndFactKey(sessionId, npcId, "player_is_thief"))
                .thenReturn(false);

        boolean result = service.recordKnowledge(sessionId, npcId, "player_is_thief");

        assertTrue(result);
        verify(npcKnowledgeFactRepository, times(1)).save(any(NpcKnowledgeFact.class));
    }

    @Test
    void recordKnowledgeSkipsSaveWhenAlreadyKnown() {
        service = new NpcServiceImpl(npcRepository, relationshipRepository, npcKnowledgeFactRepository);
        UUID sessionId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        when(npcKnowledgeFactRepository.existsBySessionIdAndNpcIdAndFactKey(sessionId, npcId, "player_is_thief"))
                .thenReturn(true);

        boolean result = service.recordKnowledge(sessionId, npcId, "player_is_thief");

        assertFalse(result);
        verify(npcKnowledgeFactRepository, never()).save(any());
    }
}

