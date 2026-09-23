package pab.rpg.service;

import pab.rpg.domain.npc.Npc;

import java.util.List;
import java.util.UUID;

public interface NpcService {

    List<Npc> getNpcsAtLocation(UUID locationId);

    int getRelationshipValue(UUID sessionId, UUID npcId);

    int changeRelationship(UUID sessionId, UUID npcId, int delta);

    // Returns true if the fact was newly learned, false if the NPC already knew it.
    boolean recordKnowledge(UUID sessionId, UUID npcId, String factKey);

    boolean knowsFact(UUID sessionId, UUID npcId, String factKey);
}
