package pab.rpg.domain.entity;

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

import java.util.UUID;

// Exactly one of characterId/npcId is set; the other stays null (enforced by chk_combat_participant_source).
@Entity
@Table(name = "combat_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CombatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID combatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CombatTeam team;

    private UUID characterId;

    private UUID npcId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private int initiative;

    @Column(nullable = false)
    private int turnOrder;

    @Column(nullable = false)
    private int actionsRemaining;

    @Column(nullable = false)
    private int healthCurrent;

    @Column(nullable = false)
    private int healthMaximum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CombatParticipantStatus status;

    public void consumeAction() {
        this.actionsRemaining = Math.max(0, this.actionsRemaining - 1);
    }

    public void resetActions() {
        this.actionsRemaining = 2;
    }

    // 0 health downs the participant per TDD MVP v0.2 section 8.5; death is out of scope for combat itself.
    public void applyDamage(int amount) {
        this.healthCurrent = Math.max(0, this.healthCurrent - amount);
        if (this.healthCurrent == 0 && this.status == CombatParticipantStatus.ACTIVE) {
            this.status = CombatParticipantStatus.DOWNED;
        }
    }

}
