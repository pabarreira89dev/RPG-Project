package pab.rpg.domain.memory;

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

// Append-only per-session log of player text + narrated outcome, used to build the "recent conversation
// summary" sent to MasterAdapter (TDD §10/§11) - distinct from GameEvent, which is the structured/auditable log.
@Entity
@Table(name = "conversation_turn")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ConversationTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private long sequence;

    @Column(nullable = false, length = 2000)
    private String playerText;

    @Column(nullable = false, length = 2000)
    private String narration;

    @Column(nullable = false)
    private Instant createdAt;

}
