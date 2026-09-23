package pab.rpg.domain.combat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

// Tracks initiative order, round/turn pointer and lifecycle of a single combat encounter for a session.
@Entity
@Table(name = "combat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Combat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CombatStatus status;

    @Column(nullable = false)
    private int roundNumber;

    @Column(nullable = false)
    private int currentTurnOrder;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant endedAt;

    public void advanceTurn(int turnOrder) {
        this.currentTurnOrder = turnOrder;
    }

    public void startNewRound(int turnOrder) {
        this.roundNumber++;
        this.currentTurnOrder = turnOrder;
    }

    public void complete(Instant now) {
        this.status = CombatStatus.COMPLETED;
        this.endedAt = now;
    }

}
