package pab.rpg.domain.npc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// Groundwork for asymmetric knowledge (GDD sec. 18-19); not yet written by any caller.
@Entity
@Table(name = "npc_knowledge_fact")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NpcKnowledgeFact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private UUID npcId;

    @Column(nullable = false, length = 100)
    private String factKey;

    @Column(nullable = false)
    private Instant learnedAt;

}
